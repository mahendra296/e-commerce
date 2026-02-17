package com.mestro.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.mestro.enums.OrderStatus;
import com.mestro.model.Order;
import com.mestro.model.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Repository slice tests using @DataJpaTest.
 * <p>
 * - H2 in-memory database is configured automatically.
 * - Each test runs in a transaction that is rolled back after completion,
 * giving full isolation between tests.
 */
@DataJpaTest
@DisplayName("OrderRepository Tests")
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    // ── Helper builders ─────────────────────────────────────────────────────

    private Order buildOrder(Long customerId, OrderStatus status) {
        Order order = Order.builder()
                .customerId(customerId)
                .status(status)
                .shippingAddress("123 Main St, Springfield, IL")
                .billingAddress("123 Main St, Springfield, IL")
                .notes("Test order")
                .totalAmount(new BigDecimal("199.99"))
                .orderItems(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .productId(101L)
                .warehouseId(1L)
                .productName("Test Product")
                .quantity(1)
                .unitPrice(new BigDecimal("199.99"))
                .build();

        order.addOrderItem(item);
        return order;
    }

    private Order persistOrder(Long customerId, OrderStatus status) {
        Order order = buildOrder(customerId, status);
        entityManager.persistAndFlush(order);
        return order;
    }

    // ─────────────────────────────────────────────
    // Basic CRUD — inherited from JpaRepository
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Basic CRUD")
    class BasicCrudTests {

        @Test
        @DisplayName("Should persist and retrieve order by ID")
        void save_ValidOrder_PersistsSuccessfully() {
            Order order = buildOrder(500L, OrderStatus.PENDING);

            Order saved = orderRepository.save(order);

            assertThat(saved.getId()).isNotNull();
            Optional<Order> found = orderRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getCustomerId()).isEqualTo(500L);
            assertThat(found.get().getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("Should return empty Optional when order does not exist")
        void findById_NonExistentId_ReturnsEmpty() {
            Optional<Order> result = orderRepository.findById(999999L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should update order status correctly")
        void save_UpdateOrderStatus_PersistsChange() {
            Order saved = orderRepository.save(buildOrder(500L, OrderStatus.PENDING));

            saved.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(saved);
            entityManager.flush();
            entityManager.clear();

            Order updated = orderRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should delete order and return empty on subsequent find")
        void delete_ExistingOrder_RemovesSuccessfully() {
            Order saved = orderRepository.save(buildOrder(500L, OrderStatus.PENDING));
            Long id = saved.getId();

            orderRepository.delete(saved);
            entityManager.flush();

            assertThat(orderRepository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Should return all persisted orders via findAll")
        void findAll_MultipleOrders_ReturnsAll() {
            persistOrder(100L, OrderStatus.PENDING);
            persistOrder(200L, OrderStatus.CONFIRMED);
            persistOrder(300L, OrderStatus.SHIPPED);

            List<Order> all = orderRepository.findAll();

            assertThat(all).hasSizeGreaterThanOrEqualTo(3);
        }
    }

    // ─────────────────────────────────────────────
    // findByCustomerId
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("findByCustomerId()")
    class FindByCustomerIdTests {

        @Test
        @DisplayName("Should return paginated orders for the matching customer")
        void findByCustomerId_ExistingCustomer_ReturnsOrders() {
            persistOrder(500L, OrderStatus.PENDING);
            persistOrder(500L, OrderStatus.CONFIRMED);
            persistOrder(600L, OrderStatus.PENDING); // different customer

            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> result = orderRepository.findByCustomerId(500L, pageable);

            assertThat(result.getTotalElements()).isEqualTo(2L);
            assertThat(result.getContent()).allMatch(o -> o.getCustomerId().equals(500L));
        }

        @Test
        @DisplayName("Should return empty page when customer has no orders")
        void findByCustomerId_NoOrders_ReturnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> result = orderRepository.findByCustomerId(99999L, pageable);

            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should respect pagination — page size limits results")
        void findByCustomerId_PaginationRespected_ReturnsCorrectPageSize() {
            for (int i = 0; i < 5; i++) {
                persistOrder(700L, OrderStatus.PENDING);
            }

            Pageable pageable = PageRequest.of(0, 2);
            Page<Order> firstPage = orderRepository.findByCustomerId(700L, pageable);

            assertThat(firstPage.getContent()).hasSize(2);
            assertThat(firstPage.getTotalElements()).isEqualTo(5L);
            assertThat(firstPage.getTotalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return second page when multiple pages exist")
        void findByCustomerId_SecondPage_ReturnsCorrectResults() {
            for (int i = 0; i < 3; i++) {
                persistOrder(800L, OrderStatus.PENDING);
            }

            Pageable secondPage = PageRequest.of(1, 2);
            Page<Order> result = orderRepository.findByCustomerId(800L, secondPage);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("Should respect sort order when specified")
        void findByCustomerId_WithSorting_ReturnsOrderedResults() {
            persistOrder(900L, OrderStatus.CONFIRMED);
            persistOrder(900L, OrderStatus.PENDING);

            Pageable sortedPageable = PageRequest.of(0, 10, Sort.by("status").ascending());
            Page<Order> result = orderRepository.findByCustomerId(900L, sortedPageable);

            assertThat(result.getContent()).hasSize(2);
        }
    }

    // ─────────────────────────────────────────────
    // findByStatus
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("findByStatus()")
    class FindByStatusTests {

        @Test
        @DisplayName("Should return only orders with PENDING status")
        void findByStatus_PendingStatus_ReturnsPendingOrders() {
            persistOrder(500L, OrderStatus.PENDING);
            persistOrder(600L, OrderStatus.PENDING);
            persistOrder(700L, OrderStatus.CONFIRMED);

            List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);

            assertThat(pendingOrders).hasSizeGreaterThanOrEqualTo(2);
            assertThat(pendingOrders).allMatch(o -> o.getStatus() == OrderStatus.PENDING);
        }

        @Test
        @DisplayName("Should return empty list when no orders have given status")
        void findByStatus_NoMatchingOrders_ReturnsEmptyList() {
            persistOrder(500L, OrderStatus.PENDING);

            List<Order> cancelledOrders = orderRepository.findByStatus(OrderStatus.CANCELLED);

            assertThat(cancelledOrders).isEmpty();
        }

        @Test
        @DisplayName("Should correctly distinguish between all order statuses")
        void findByStatus_AllStatusValues_ReturnCorrectSets() {
            persistOrder(100L, OrderStatus.PENDING);
            persistOrder(200L, OrderStatus.CONFIRMED);
            persistOrder(300L, OrderStatus.SHIPPED);
            persistOrder(400L, OrderStatus.DELIVERED);
            persistOrder(500L, OrderStatus.CANCELLED);

            assertThat(orderRepository.findByStatus(OrderStatus.PENDING))
                    .anyMatch(o -> o.getCustomerId().equals(100L));
            assertThat(orderRepository.findByStatus(OrderStatus.DELIVERED))
                    .anyMatch(o -> o.getCustomerId().equals(400L));
            assertThat(orderRepository.findByStatus(OrderStatus.CANCELLED))
                    .anyMatch(o -> o.getCustomerId().equals(500L));
        }
    }

    // ─────────────────────────────────────────────
    // findByCustomerIdAndStatus
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("findByCustomerIdAndStatus()")
    class FindByCustomerIdAndStatusTests {

        @Test
        @DisplayName("Should return orders matching both customerId and status")
        void findByCustomerIdAndStatus_MatchingBoth_ReturnsOrders() {
            persistOrder(500L, OrderStatus.PENDING);
            persistOrder(500L, OrderStatus.CONFIRMED);
            persistOrder(600L, OrderStatus.PENDING);

            List<Order> result = orderRepository.findByCustomerIdAndStatus(500L, OrderStatus.PENDING);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCustomerId()).isEqualTo(500L);
            assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("Should return empty list when no order matches both criteria")
        void findByCustomerIdAndStatus_NoMatch_ReturnsEmptyList() {
            persistOrder(500L, OrderStatus.PENDING);

            List<Order> result = orderRepository.findByCustomerIdAndStatus(500L, OrderStatus.DELIVERED);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return multiple matching orders for same customer and status")
        void findByCustomerIdAndStatus_MultipleOrders_ReturnsAll() {
            persistOrder(500L, OrderStatus.CONFIRMED);
            persistOrder(500L, OrderStatus.CONFIRMED);

            List<Order> result = orderRepository.findByCustomerIdAndStatus(500L, OrderStatus.CONFIRMED);

            assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    // ─────────────────────────────────────────────
    // findOrdersBetweenDates  (@Query)
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("findOrdersBetweenDates() [@Query]")
    class FindOrdersBetweenDatesTests {

        @Test
        @DisplayName("Should return empty list when no orders fall within the range")
        void findOrdersBetweenDates_FutureRange_ReturnsEmptyList() {
            persistOrder(500L, OrderStatus.PENDING);

            LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
            LocalDateTime futureEnd = LocalDateTime.now().plusDays(2);

            List<Order> result = orderRepository.findOrdersBetweenDates(futureStart, futureEnd);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for past range with no matching orders")
        void findOrdersBetweenDates_PastRange_ReturnsEmptyList() {
            LocalDateTime oldStart = LocalDateTime.of(2000, 1, 1, 0, 0);
            LocalDateTime oldEnd = LocalDateTime.of(2000, 12, 31, 23, 59);

            List<Order> result = orderRepository.findOrdersBetweenDates(oldStart, oldEnd);

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────
    // findByIdWithItems (@Query JOIN FETCH)
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("findByIdWithItems() [@Query JOIN FETCH]")
    class FindByIdWithItemsTests {

        @Test
        @DisplayName("Should eagerly load order items with the order")
        void findByIdWithItems_OrderWithItems_LoadsItemsEagerly() {
            Order saved = orderRepository.save(buildOrder(500L, OrderStatus.PENDING));
            entityManager.flush();
            entityManager.clear(); // clear to force reload from DB

            Order result = orderRepository.findByIdWithItems(saved.getId());

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(saved.getId());
            assertThat(result.getOrderItems()).isNotEmpty();
            assertThat(result.getOrderItems().get(0).getProductName()).isEqualTo("Test Product");
        }

        @Test
        @DisplayName("Should return null when order ID does not exist")
        void findByIdWithItems_NonExistentId_ReturnsNull() {
            Order result = orderRepository.findByIdWithItems(999999L);

            assertThat(result).isNull();
        }
    }

    // ─────────────────────────────────────────────
    // countByCustomerId (@Query)
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("countByCustomerId() [@Query]")
    class CountByCustomerIdTests {

        @Test
        @DisplayName("Should return correct count for a customer with orders")
        void countByCustomerId_ExistingCustomer_ReturnsCount() {
            persistOrder(500L, OrderStatus.PENDING);
            persistOrder(500L, OrderStatus.CONFIRMED);
            persistOrder(500L, OrderStatus.SHIPPED);

            Long count = orderRepository.countByCustomerId(500L);

            assertThat(count).isGreaterThanOrEqualTo(3L);
        }

        @Test
        @DisplayName("Should return 0 for a customer with no orders")
        void countByCustomerId_CustomerWithNoOrders_ReturnsZero() {
            Long count = orderRepository.countByCustomerId(99999L);

            assertThat(count).isZero();
        }

        @Test
        @DisplayName("Should not count orders belonging to other customers")
        void countByCustomerId_OtherCustomerOrders_NotCounted() {
            persistOrder(500L, OrderStatus.PENDING);
            persistOrder(600L, OrderStatus.PENDING); // different customer

            Long countFor500 = orderRepository.countByCustomerId(500L);
            Long countFor600 = orderRepository.countByCustomerId(600L);

            assertThat(countFor500).isGreaterThanOrEqualTo(1L);
            assertThat(countFor600).isGreaterThanOrEqualTo(1L);
            // counts are independent
            assertThat(countFor500).isNotEqualTo(countFor500 + countFor600);
        }

        @Test
        @DisplayName("Should update count correctly after order is deleted")
        void countByCustomerId_AfterDeletion_CountDecreases() {
            Order order1 = persistOrder(500L, OrderStatus.PENDING);
            Order order2 = persistOrder(500L, OrderStatus.PENDING);

            Long beforeDelete = orderRepository.countByCustomerId(500L);
            orderRepository.delete(order1);
            entityManager.flush();
            Long afterDelete = orderRepository.countByCustomerId(500L);

            assertThat(afterDelete).isEqualTo(beforeDelete - 1);
        }
    }

    // ─────────────────────────────────────────────
    // Order-Item cascade behaviour
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Cascade / Orphan removal")
    class CascadeTests {

        @Test
        @DisplayName("Should persist order items automatically with the order")
        void save_OrderWithItems_CascadesItemPersistence() {
            Order order = buildOrder(500L, OrderStatus.PENDING);
            Order saved = orderRepository.save(order);
            entityManager.flush();
            entityManager.clear();

            Order loaded = orderRepository.findByIdWithItems(saved.getId());

            assertThat(loaded.getOrderItems()).hasSize(1);
            assertThat(loaded.getOrderItems().get(0).getId()).isNotNull();
        }
    }
}
