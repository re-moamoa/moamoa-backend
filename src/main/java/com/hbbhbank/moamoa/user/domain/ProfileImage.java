package com.hbbhbank.moamoa.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="profile_image")
public class ProfileImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "profile_image_id")
  private Long id;

  @Column(name="profile_image_url", nullable = false)
  private String url;

  @Builder
  public ProfileImage(String url) {
    this.url = url;
  }
}