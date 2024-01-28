package thainguyen.dto.ghtk;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
public class GhtkForm {

    private List<GhtkProductForm> products = new ArrayList<>();
    private GhtkOrderForm order;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GhtkProductForm {
        private String name;
        private Double weight;
        private Integer quantity;
        private Long product_code;

        public GhtkProductForm(String name, Double weight, Integer quantity, Long product_code) {
            this.name = name;
            this.weight = weight;
            this.quantity = quantity;
            this.product_code = product_code;
        }
    }

    @Getter
    @Setter
    public static class GhtkOrderForm {
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
        private @NotNull Integer pick_money;
        private @NotNull Integer value;

    }
}
