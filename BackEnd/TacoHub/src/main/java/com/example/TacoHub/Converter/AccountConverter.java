package com.example.TacoHub.Converter;

import com.example.TacoHub.Dto.AccountDto;
import com.example.TacoHub.Entity.AccountEntity;

public class AccountConverter {

    public static AccountEntity toEntity(AccountDto accountDTO)
    {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setEmailId(accountDTO.getEmailId());
        accountEntity.setPassword(accountDTO.getPassword());
        accountEntity.setName(accountDTO.getName());
        accountEntity.setRole(accountDTO.getRole());
        return accountEntity;
    }

    public static AccountDto toDTO(AccountEntity accountEntity)
    {
        AccountDto accountDTO = new AccountDto();
        accountDTO.setEmailId(accountEntity.getEmailId());
        accountDTO.setPassword(accountEntity.getPassword());
        accountDTO.setName(accountEntity.getName());
        accountDTO.setRole(accountEntity.getRole());
        return accountDTO;
    }



}
