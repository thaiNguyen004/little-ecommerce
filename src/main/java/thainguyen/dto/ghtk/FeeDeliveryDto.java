package thainguyen.dto.ghtk;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@ToString
public class FeeDeliveryDto {
    private boolean success;
//    private String message;
    private Fee fee;

    public FeeDeliveryDto(boolean success, Fee fee) {
        this.success = success;
        this.fee = fee;
    }

    @Getter
    @Setter
    @ToString
    public static class Fee {
//        private String name;
        private Integer fee;
        private Integer insurance_fee;
//        private boolean delivery;
        /*private List<ExtFee> extFees = new ArrayList<>();

        @Getter
        @Setter
        @ToString
        public static class ExtFee {
            private String display;
            private String title;
            private Integer amount;
            private String type;
        }*/
    }
}
