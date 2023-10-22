package org.project.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubArgs {

    private byte[] topic;

    private byte[] subId;

}
