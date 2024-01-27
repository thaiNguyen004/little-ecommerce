package thainguyen.dto.ghtk;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/*{
  "success": true,
  "message": "",
  "order": {
    "partner_id": "123123a",
    "label": "S1.A1.1737345",
    "area": "1",
    "fee": "30400",
    "insurance_fee": "15000",
    "estimated_pick_time": "Sáng 2017-07-01",
    "estimated_deliver_time": "Chiều 2017-07-01",
    "products": [],
    "status_id": 2
  }
}*/

@Getter
@Setter
public class OrderGHTKDto {

    private Boolean success;
    private String message;
    private OrderResponseInfo order;
    private String warning_message;


    @Getter
    @Setter
    public static class OrderResponseInfo {
        private String label;
        private BigDecimal fee;
        private BigDecimal insurance_fee;
        private String estimated_pick_time;
        private String estimated_deliver_time;
        private Integer status_id;
        private Integer tracking_id;
        private String sorting_code;
    }
}
