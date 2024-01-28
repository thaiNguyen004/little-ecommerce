package thainguyen.dto.order;

import static thainguyen.dto.order.OrderDto.LineItemDto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import thainguyen.domain.DetailProduct;
import thainguyen.domain.Discount;
import thainguyen.domain.LineItem;
import thainguyen.domain.Order;
import thainguyen.domain.valuetypes.Price;
import thainguyen.service.detailproduct.DetailProductService;
import thainguyen.service.discount.DiscountService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class OrderMapperImpl implements OrderMapper {

    private final DiscountService discountService;
    private final DetailProductService detailProductService;

    public OrderMapperImpl(DiscountService discountService
            , DetailProductService detailProductService) {

        this.discountService = discountService;
        this.detailProductService = detailProductService;
    }

    @Override
    public Order convertOrderDtoToEntity(OrderDto orderDto) {
        if (orderDto == null) return null;
        Order order = new Order();
        order.setAddress(orderDto.getAddress());
        List<Long> discountsDto = orderDto.getDiscounts();
        if (discountsDto != null && !discountsDto.isEmpty()) {
            for (Long discountId : discountsDto) {
                Optional<Discount> discountOpt = discountService.findById(discountId);
                if (discountOpt.isEmpty()) return null;
                order.addDiscount(discountOpt.get());
            }
        }
        List<LineItemDto> lineItemDto =  orderDto.getLineItems();
        if (lineItemDto == null || lineItemDto.isEmpty()) return null;
        for (LineItemDto ltDto : lineItemDto) {
            LineItem lt = convertLineItemDtoToEntity(ltDto);
            if (lt == null) return null;
            order.addLineItem(lt);
        }
        return order;
    }

    private LineItem convertLineItemDtoToEntity(LineItemDto lineItemDto) {
        if (lineItemDto == null) return null;
        Optional<DetailProduct> detailProductOpt = detailProductService.findById(lineItemDto.getDetailProductId());
        if (detailProductOpt.isEmpty()) return null;
        LineItem lineItem = new LineItem();
        lineItem.setQuantity(lineItemDto.getQuantity());
        DetailProduct dp = detailProductOpt.get();
        lineItem.setDetailProduct(dp);
        lineItem.setAmount(dp.getPrice());
        // calc totalPrice
        Price price = dp.getPrice();
        BigDecimal value = price.getValue();
        BigDecimal quantity = BigDecimal.valueOf(lineItem.getQuantity());
        Double totalWeight = dp.getWeight() * quantity.doubleValue();
        lineItem.setTotalWeight(
                BigDecimal.valueOf(totalWeight).setScale(3, RoundingMode.HALF_UP)
                        .doubleValue()
        );

        lineItem.setTotalPrice(new Price(
                value.multiply(quantity).setScale(1, RoundingMode.HALF_UP),
                Currency.getInstance("VND")
        ));
        return lineItem;
    }

}
