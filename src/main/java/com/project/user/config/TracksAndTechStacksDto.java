package com.project.user.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TracksAndTechStacksDto {
    private List<String> tracks;
    private List<String> techStacks;
}
