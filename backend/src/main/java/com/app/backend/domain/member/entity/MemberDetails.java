package com.app.backend.domain.member.entity;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

@Getter
public class MemberDetails implements UserDetails {
	private final Long id;
	private final String username;
	private final String password;
	private final String nickname;
	private final String provider;
	private final String role;
	private final String createdAt;
	private final String modifiedAt;
	private final boolean disabled;

	public MemberDetails(Member member) {
		this.id = member.getId();
		this.username = member.getUsername();
		this.nickname = member.getNickname();
		this.role = member.getRole();
		this.disabled = member.getDisabled();
		this.password = member.getPassword();
		this.provider = String.valueOf(member.getProvider());
		this.createdAt = String.valueOf(member.getCreatedAt());
		this.modifiedAt = String.valueOf(member.getModifiedAt());
	}

	public static MemberDetails of(Member member) {
		return new MemberDetails(member);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority(role));
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return !this.disabled;
	}
}
