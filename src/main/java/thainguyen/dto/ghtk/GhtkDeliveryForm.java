package thainguyen.dto.ghtk;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GhtkDeliveryForm {

    private GhtkAddress address = new GhtkAddress();
    private @NotNull Integer weight; // gam
    private @NotNull Integer value;

    private @NotNull String pick_name;
    private @NotNull String name;

    @Getter
    @Setter
    public static class GhtkAddress {
        private @NotNull String pick_province;
        private @NotNull String pick_district;
        private @NotNull String pick_ward;
        private @NotNull String pick_address;
        private @NotNull String province;
        private @NotNull String district;
        private @NotNull String ward;
        private @NotNull String address;
        private @NotNull String hamlet;
        private @NotNull String tel;
        private @NotNull String pick_tel;
    }
}
