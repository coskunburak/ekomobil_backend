package com.ekomobil.api.rest.publicapi;

import com.ekomobil.domain.dto.card.CreateCardRequest;
import com.ekomobil.domain.dto.card.CardDto;
import com.ekomobil.domain.dto.card.ChargeRequest;
import com.ekomobil.domain.dto.card.TopUpRequest;
import com.ekomobil.domain.dto.card.TransferRequest;
import com.ekomobil.domain.dto.card.TransactionDto;

import com.ekomobil.security.UserPrincipal;
import com.ekomobil.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cards")
public class CardController {

    private final CardService service;

    @GetMapping
    public List<CardDto> myCards(@AuthenticationPrincipal UserPrincipal me) {
        return service.myCards(me.getId());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CardDto create(@AuthenticationPrincipal UserPrincipal me,
                          @Valid @RequestBody CreateCardRequest req) {
        return service.createCard(me.getId(), req);
    }

    @GetMapping("/{id}")
    public CardDto get(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long id) {
        return service.getCard(me.getId(), id);
    }

    @GetMapping("/{id}/history")
    public List<TransactionDto> history(@AuthenticationPrincipal UserPrincipal me,
                                        @PathVariable Long id) {
        return service.history(me.getId(), id);
    }

    @PostMapping("/{id}/topup")
    public CardDto topup(@AuthenticationPrincipal UserPrincipal me,
                         @PathVariable Long id,
                         @Valid @RequestBody TopUpRequest req) {
        return service.topUp(me.getId(), id, req);
    }

    @PostMapping("/{id}/charge")
    public CardDto charge(@AuthenticationPrincipal UserPrincipal me,
                          @PathVariable Long id,
                          @Valid @RequestBody ChargeRequest req) {
        return service.charge(me.getId(), id, req);
    }

    @PostMapping("/{id}/transfer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transfer(@AuthenticationPrincipal UserPrincipal me,
                         @PathVariable Long id,
                         @Valid @RequestBody TransferRequest req) {
        service.transfer(me.getId(), id, req);
    }

    @PostMapping("/{id}/block")
    public CardDto block(@AuthenticationPrincipal UserPrincipal me,
                         @PathVariable Long id) {
        return service.block(me.getId(), id);
    }
}
