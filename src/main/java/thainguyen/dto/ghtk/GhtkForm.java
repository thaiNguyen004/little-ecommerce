package thainguyen.dto.ghtk;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * is_freeship: Integer - Freeship cho người nhận hàng.
 * Nếu bằng 1 COD sẽ chỉ thu người nhận hàng số tiền bằng pick_money,
 * nếu bằng 0 COD sẽ thu tiền người nhận số tiền bằng pick_money + phí ship của đơn hàng,
 * giá trị mặc định bằng 0
 * */

@Data
public class GhtkForm {

    private List<GhtkProduct> products = new ArrayList<>();
    private GhtkOrder order;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GhtkProduct {
        private String name;
        private Double weight;
        private Integer quantity;
        private Long product_code;

        public GhtkProduct(String name, Double weight, Integer quantity, Long product_code) {
            this.name = name;
            this.weight = weight;
            this.quantity = quantity;
            this.product_code = product_code;
        }
    }

    @Getter
    @Setter
    public static class GhtkOrder {
        private @NotNull String id;
        private @NotNull String pick_name;
        private @NotNull String pick_province;
        private @NotNull String pick_district;
        private @NotNull String pick_ward;
        private @NotNull String pick_address;
        private @NotNull String pick_tel;
        private @NotNull String name;
        private @NotNull String email;
        private @NotNull String province;
        private @NotNull String district;
        private @NotNull String ward;
        private @NotNull String address;
        private String hamlet;
        private @NotNull String tel;
        private Integer is_freeship;
        private @NotNull Integer pick_money; // Số tiền CoD. Nếu bằng 0 thì không thu tiền CoD. Tính theo VNĐ
        private @NotNull Integer value;

    }
}
