package thainguyen.controller.conf;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomResponse {
    private String success;
    private String message;
    private String status_code;
}
