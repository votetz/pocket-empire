package com.pocketempire.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CasusBelliConfig {
    private String id;
    private String description;
    private int cooldown;
}
