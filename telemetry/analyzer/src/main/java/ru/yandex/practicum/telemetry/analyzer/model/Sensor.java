package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "sensors")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Sensor {

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    @Column(name = "id", nullable = false)
    private String id;

    @NonNull
    @Column(name = "hub_id", nullable = false)
    private String hubId;
}
