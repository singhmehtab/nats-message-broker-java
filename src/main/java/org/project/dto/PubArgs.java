package org.project.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PubArgs {

    private byte[] topic;

    private Integer size;

    private byte[] payload;

}
