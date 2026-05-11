package cn.y.yai.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserDeleteRequest implements Serializable {

    private Long id;

    @Serial
    private static final long serialVersionUID = 1L;

}
