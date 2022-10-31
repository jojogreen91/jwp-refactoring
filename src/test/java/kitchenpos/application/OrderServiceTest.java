package kitchenpos.application;

import static kitchenpos.DomainFixture.createMenu;
import static kitchenpos.DomainFixture.createOrder;
import static kitchenpos.DomainFixture.createOrderTable;
import static kitchenpos.DomainFixture.forUpdateEmpty;
import static kitchenpos.DomainFixture.forUpdateStatus;
import static kitchenpos.DomainFixture.메뉴그룹1;
import static kitchenpos.DomainFixture.메뉴그룹2;
import static kitchenpos.DomainFixture.피자;
import static kitchenpos.DomainFixture.후라이드치킨;
import static kitchenpos.domain.OrderStatus.MEAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import kitchenpos.DomainFixture;
import kitchenpos.domain.Menu;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.dto.OrderDto;
import kitchenpos.dto.OrderTableDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OrderServiceTest extends ServiceTest {

    @Test
    @DisplayName("주문을 등록한다.")
    void create() {
        // given
        final OrderTable table = 주문테이블등록(createOrderTable(3, false));
        final Menu menu = 메뉴등록(createMenu("양념치킨메뉴", 10_000, 메뉴그룹등록(메뉴그룹1), 상품등록(피자)));

        final Order order = createOrder(table, menu);

        // when
        final Order createdOrder = orderService.create(new OrderDto(order));

        // then
        assertAll(
                () -> assertThat(createdOrder.getId()).isNotNull(),
                () -> assertThat(createdOrder.getOrderStatus()).isEqualTo(OrderStatus.COOKING.name())
        );
    }

    @Test
    @DisplayName("create -> 주문 항목에 기재된 메뉴가 존재하지 않을 경우 예외가 발생한다.")
    void create_noMenu_throwException() {
        // given
        final OrderTable table = 주문테이블등록(createOrderTable(3, false));
        final Menu menu = createMenu("양념치킨메뉴", 10_000, 메뉴그룹등록(메뉴그룹1), 상품등록(피자));
        final Menu notRegisteredMenu =
                new Menu(999L, menu.getName(), menu.getPrice(), menu.getMenuGroupId(), menu.getMenuProducts());
        final Order order = createOrder(table, notRegisteredMenu);

        // when & then
        assertThatThrownBy(() -> orderService.create(new OrderDto(order)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("create -> 주문 테이블이 비어있을 경우 예외가 발생한다.")
    void create_emptyTable_throwException() {
        // given
        final OrderTable table = 주문테이블등록(createOrderTable(3, false));
        final Menu menu = 메뉴등록(createMenu("양념치킨메뉴", 10_000, 메뉴그룹등록(메뉴그룹1), 상품등록(피자)));

        tableService.changeEmpty(table.getId(), new OrderTableDto(forUpdateEmpty(true)));

        final Order order = createOrder(table, menu);

        // when & then
        assertThatThrownBy(() -> orderService.create(new OrderDto(order)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("주문 목록을 조회한다.")
    void list() {
        // given
        final OrderTable table1 = 주문테이블등록(createOrderTable(3, false));
        final Menu menu1 = 메뉴등록(createMenu("양념치킨메뉴", 10_000, 메뉴그룹등록(메뉴그룹1), 상품등록(피자)));

        주문등록(createOrder(table1, menu1));

        final OrderTable table2 = 주문테이블등록(createOrderTable(3, false));
        final Menu menu2 = 메뉴등록(createMenu("후라이드메뉴", 8_000, 메뉴그룹등록(메뉴그룹2), 상품등록(후라이드치킨)));

        주문등록(createOrder(table2, menu2));

        // when
        final List<Order> actual = orderService.list();

        // then
        assertThat(actual).hasSize(2);
    }

    @Test
    @DisplayName("주문 상태를 변경한다.")
    void changeOrderStatus() {
        // given
        final OrderTable table = 주문테이블등록(createOrderTable(3, false));
        final Menu menu = 메뉴등록(createMenu("양념치킨메뉴", 10_000, 메뉴그룹등록(메뉴그룹1), 상품등록(피자)));

        final Order order = 주문등록(createOrder(table, menu));

        // when
        final Order actual = orderService.changeOrderStatus(order.getId(), new OrderDto(forUpdateStatus("MEAL")));

        // then
        assertThat(actual.getOrderStatus()).isEqualTo(MEAL.name());
    }

    @Test
    @DisplayName("changeOrderStatus -> 주문이 존재하지 않으면 예외가 발생한다.")
    void changeOrderStatus_noOrder_throwException() {
        // given
        final OrderTable table = 주문테이블등록(createOrderTable(3, false));
        final Menu menu = 메뉴등록(createMenu("탕수육_메뉴", 10_000, 메뉴그룹등록(메뉴그룹1), 상품등록(피자)));

        final Order notRegisteredOrder = createOrder(table, menu);

        // when & then
        assertThatThrownBy(
                () -> orderService.changeOrderStatus(notRegisteredOrder.getId(), new OrderDto(forUpdateStatus("MEAL")))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("changeOrderStatus -> 주문이 이미 계산 완료된 경우 예외가 발생한다.")
    void changeOrderStatus_alreadyCompletion_throwException() {
        // given
        final OrderTable table = 주문테이블등록(createOrderTable(3, false));
        final Menu menu = 메뉴등록(createMenu("양념치킨메뉴", 10_000, 메뉴그룹등록(메뉴그룹1), 상품등록(피자)));

        final Order order = 주문등록(createOrder(table, menu));
        orderService.changeOrderStatus(order.getId(), new OrderDto(forUpdateStatus(OrderStatus.COMPLETION.name())));

        // when & then
        assertThatThrownBy(
                () -> orderService.changeOrderStatus(order.getId(), new OrderDto(forUpdateStatus("COOKING")))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("changeOrderStatus -> 유효하지 않은 주문상태를 입력한 경우 예외가 발생한다.")
    void changeOrderStatus_invalidOrderStatus_throwException() {
        // given
        final OrderTable table = 주문테이블등록(createOrderTable(3, false));
        final Menu menu = 메뉴등록(createMenu("탕수육_메뉴", 10_000, 메뉴그룹등록(메뉴그룹1), 상품등록(피자)));

        final Order order = 주문등록(createOrder(table, menu));

        final Order forUpdate = DomainFixture.forUpdateStatus("TEST");

        // when & then
        assertThatThrownBy(() -> orderService.changeOrderStatus(order.getId(), new OrderDto(forUpdate)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
