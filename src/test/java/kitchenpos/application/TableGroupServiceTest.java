package kitchenpos.application;

import static kitchenpos.DomainFixture.createOrderTable;
import static kitchenpos.DomainFixture.createTableGroup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.dto.TableGroupDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TableGroupServiceTest extends ServiceTest {

    @Test
    @DisplayName("테이블을 단체로 지정할 수 있다.")
    void create() {
        // given
        final OrderTable table1 = 주문테이블등록(createOrderTable(3, true));
        final OrderTable table2 = 주문테이블등록(createOrderTable(5, true));

        // when
        final TableGroup tableGroup = tableGroupService.create(new TableGroupDto(createTableGroup(table1, table2)));
        final OrderTable actual = orderTableDao.findById(table1.getId())
                .orElseThrow();

        // then
        assertAll(
                () -> assertThat(tableGroup.getId()).isNotNull(),
                () -> assertThat(actual.getTableGroupId()).isNotNull(),
                () -> assertThat(tableGroup.getOrderTables()).hasSize(2)
        );
    }

    @Test
    @DisplayName("create -> 빈 테이블이 아닐 경우 예외가 발생한다.")
    void create_emptyTable_throwException() {
        // given
        final OrderTable table1 = 주문테이블등록(createOrderTable(3, true));
        final OrderTable table2 = 주문테이블등록(createOrderTable(5, false));

        // when & then
        assertThatThrownBy(() -> tableGroupService.create(new TableGroupDto(createTableGroup(table1, table2))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("create -> 입력한 주문 테이블이 이미 단체로 지정된 경우 예외가 발생한다.")
    void create_alreadyGrouped_throwException() {
        // given
        final OrderTable groupedTable1 = 주문테이블등록(createOrderTable(3, true));
        final OrderTable groupedTable2 = 주문테이블등록(createOrderTable(3, true));
        tableGroupService.create(new TableGroupDto(createTableGroup(groupedTable1, groupedTable2)));

        final OrderTable table = 주문테이블등록(createOrderTable(5, true));

        // when & then
        assertThatThrownBy(() -> tableGroupService.create(new TableGroupDto(createTableGroup(groupedTable1, table))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("단체지정을 해제한다.")
    void ungroup() {
        // given
        final OrderTable groupedTable1 = 주문테이블등록(createOrderTable(3, true));
        final OrderTable groupedTable2 = 주문테이블등록(createOrderTable(3, true));
        TableGroup tableGroup =
                tableGroupService.create(new TableGroupDto(createTableGroup(groupedTable1, groupedTable2)));

        // when
        tableGroupService.ungroup(tableGroup.getId());
        final OrderTable actual = orderTableDao.findById(groupedTable1.getId())
                .orElseThrow();

        // then
        assertThat(actual.getTableGroupId()).isNull();
    }
}
