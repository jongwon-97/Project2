package com.kosmo.nexus.dto;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class EventDTO {
    private int eventId;
    private String eventTitle;
    private String eventContent;
    private String memberId;
}
