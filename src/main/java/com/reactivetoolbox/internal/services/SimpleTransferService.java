package com.reactivetoolbox.internal.services;

import com.reactivetoolbox.api.request.TransferRequest;
import com.reactivetoolbox.api.response.TransferResponse;
import com.reactivetoolbox.api.service.TransferService;
import com.reactivetoolbox.domain.Account;
import com.reactivetoolbox.domain.Operation;
import com.reactivetoolbox.domain.Transfer;
import com.reactivetoolbox.internal.repository.AccountRepository;
import com.reactivetoolbox.internal.repository.HistoryRepository;
import org.reactivetoolbox.core.async.Promise;
import org.reactivetoolbox.core.lang.Result;
import org.reactivetoolbox.core.lang.Tuple;
import org.reactivetoolbox.core.lang.Tuple.Tuple2;

import static com.reactivetoolbox.api.response.TransferResponse.success;
import static org.reactivetoolbox.core.lang.Result.zip;

public class SimpleTransferService implements TransferService {
    private final HistoryRepository historyRepository;
    private final AccountRepository accountRepository;

    public SimpleTransferService(final HistoryRepository historyRepository,
                                 final AccountRepository accountRepository) {
        this.historyRepository = historyRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Promise<TransferResponse> transfer(final TransferRequest request) {
        final Transfer transfer = Transfer.from(request);

        return transfer.accounts()
                       .map(accountRepository::find, accountRepository::find)
                       .map(Promise::all)
                       .flatMap(accounts -> applyOperations(transfer, accounts))
                       .chainMap(accountsAndOps -> accountsAndOps.map(this::saveAccounts, this::saveOperations)
                                                                 .map(Promise::all))
                       .map($ -> success(transfer.id()));
    }

    private Result<Tuple2<Tuple2<Account, Account>, Tuple2<Operation, Operation>>> applyOperations(final Transfer transfer,
                                                                                                   final Tuple2<Account, Account> accounts) {
        return Operation.split(transfer)
                        .map((opFrom, opTo) -> accounts.map((accFrom, accTo) -> zip(accFrom.apply(opFrom), accTo.apply(opTo))))
                        .map(Tuple::rotate);
    }

    private Promise<Tuple2<Operation, Operation>> saveOperations(final Tuple2<Operation, Operation> operations) {

        return operations.map(historyRepository::save, historyRepository::save)
                         .map(Promise::all);
    }

    private Promise<Tuple2<Account, Account>> saveAccounts(final Tuple2<Account, Account> accounts) {
        return accounts.map(accountRepository::save, accountRepository::save)
                       .map(Promise::all);
    }
}
