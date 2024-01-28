package thainguyen.dto.ghtk;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderGHTKDto {

    private Boolean success;
    private OrderResponseInfo order;

    @Getter
    @Setter
    public static class OrderResponseInfo {
        private String label;
        private Integer fee;
        private Integer insurance_fee;
        private String estimated_pick_time;
        private String estimated_deliver_time;
        private Integer status_id;
        private Integer tracking_id;
        private String sorting_code;
    }
}
