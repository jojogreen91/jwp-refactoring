package kitchenpos.application;

import static kitchenpos.DomainFixture.createMenu;
import static kitchenpos.DomainFixture.메뉴그룹1;
import static kitchenpos.DomainFixture.메뉴그룹2;
import static kitchenpos.DomainFixture.양념치킨;
import static kitchenpos.DomainFixture.피자;
import static kitchenpos.DomainFixture.후라이드치킨;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.Product;
import kitchenpos.dto.MenuDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MenuServiceTest extends ServiceTest {

    @Test
    @DisplayName("메뉴를 등록한다.")
    void create() {
        // given
        final MenuGroup menuGroup = 메뉴그룹등록(메뉴그룹1);
        final Product product = 상품등록(피자);
        final Menu menu = createMenu("후라이드치킨메뉴", 1_000, menuGroup, product);

        // when
        final Menu createdMenu = menuService.create(new MenuDto(menu));

        // then
        final Long createdMenuId = createdMenu.getId();
        assertAll(
                () -> assertThat(createdMenuId).isNotNull(),
                () -> assertThat(menuDao.findById(createdMenuId)).isPresent()
        );
    }

    @Test
    @DisplayName("create -> 메뉴 그룹이 존재하지 않으면 예외가 발생한다.")
    void create_noGroup_throwException() {
        // given
        final MenuGroup notRegisteredMenuGroup = 메뉴그룹1;
        final Product product = 상품등록(피자);
        final Menu menu = createMenu("양념치킨메뉴", 1_000, notRegisteredMenuGroup, product);

        // when & then
        assertThatThrownBy(() -> menuService.create(new MenuDto(menu)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("메뉴 목록을 조회한다.")
    void list() {
        // given
        final MenuGroup menuGroup1 = 메뉴그룹등록(메뉴그룹1);
        final Product product1 = 상품등록(후라이드치킨);
        final Menu menu1 = createMenu("후라이드치킨메뉴", 1_000, menuGroup1, product1);
        메뉴등록(menu1);

        final MenuGroup menuGroup2 = 메뉴그룹등록(메뉴그룹2);
        final Product product2 = 상품등록(양념치킨);
        final Menu menu2 = createMenu("양념치킨메뉴", 1_000, menuGroup2, product2);
        메뉴등록(menu2);

        // when
        final List<Menu> actual = menuService.list();

        // then
        assertThat(actual).hasSize(2);
    }
}
