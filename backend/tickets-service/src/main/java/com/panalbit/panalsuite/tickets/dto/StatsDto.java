package com.panalbit.panalsuite.tickets.dto;

public record StatsDto(long total, long open, long inProgress, long pending, long resolved, long closed,
                       long critical, long high, long myAssigned) {
}
