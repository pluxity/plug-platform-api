package com.pluxity.global.auditing;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "revision_info")
@RevisionEntity
@Entity
public class CustomRevisionEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    @EqualsAndHashCode.Include
    @Column(name = "revision_id")
    private long id;

    @EqualsAndHashCode.Include
    @RevisionTimestamp
    @Column(name = "revision_timestamp")
    private long timestamp;
}
