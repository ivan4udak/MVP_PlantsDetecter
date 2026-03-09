package com.plantidentifier.service;

import com.plantidentifier.dto.request.GuestSessionRequest;
import com.plantidentifier.dto.response.GuestSessionResponse;

public interface ISessionService {
    GuestSessionResponse createGuestSession(GuestSessionRequest request);
}