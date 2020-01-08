package io.pillopl.library.lending.patron.application.hold;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.HoldDuration;
import io.pillopl.library.lending.patron.model.NumberOfDays;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Option;

import java.time.Instant;

import lombok.NonNull;
import lombok.Value;


/**
 * 借阅预定命令
 */
@Value
public class PlaceOnHoldCommand {
    @NonNull Instant timestamp;

    ////////////////////////////借阅预定要素///////////////
    @NonNull PatronId patronId;

    @NonNull LibraryBranchId libraryId;

    @NonNull BookId bookId;

    // 借阅预定有效期限
    Option<Integer> noOfDays;


    static PlaceOnHoldCommand closeEnded(PatronId patronId, LibraryBranchId libraryBranchId, BookId bookId, int forDays) {
        return new PlaceOnHoldCommand(Instant.now(), patronId, libraryBranchId, bookId, Option.of(forDays));
    }


    static PlaceOnHoldCommand openEnded(PatronId patronId, LibraryBranchId libraryBranchId, BookId bookId) {
        return new PlaceOnHoldCommand(Instant.now(), patronId, libraryBranchId, bookId, Option.none());
    }


    HoldDuration getHoldDuration() {
        return noOfDays
                .map(NumberOfDays::of)
                .map(HoldDuration::closeEnded)
                .getOrElse(HoldDuration.openEnded());
    }
}
