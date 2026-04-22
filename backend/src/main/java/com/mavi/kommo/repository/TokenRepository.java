package com.mavi.kommo.repository;

import com.mavi.kommo.domain.KommoToken;

import java.util.Optional;

public interface TokenRepository {

    Optional<KommoToken> findToken();

    void saveToken(KommoToken token);

    void deleteToken();
}
