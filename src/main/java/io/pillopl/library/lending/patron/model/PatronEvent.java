package io.pillopl.library.lending.patron.model;

import io.pillopl.library.commons.events.DomainEvent;
import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * 借阅人事件
 */
public interface PatronEvent extends DomainEvent {

    default PatronId patronId() {
        return new PatronId(getPatronId());
    }

    UUID getPatronId();

    default UUID getAggregateId() {
        return getPatronId();
    }

    default List<DomainEvent> normalize() {
        return List.of(this);
    }


    // 借阅人创建事件
    @Value
    class PatronCreated implements PatronEvent {
        @NonNull UUID eventId = UUID.randomUUID();

        @NonNull Instant when;

        // 借阅人id
        @NonNull UUID patronId;

        // 借阅人类型
        @NonNull PatronType patronType;

        public static PatronCreated now(PatronId patronId, PatronType type) {
            return new PatronCreated(Instant.now(), patronId.getPatronId(), type);
        }
    }


    // 借阅人借阅事件
    @Value
    class BookPlacedOnHold implements PatronEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;

        //////////////////////事件要素////////////////////
        // 借阅人id
        @NonNull UUID patronId;

        // 借阅书籍id
        @NonNull UUID bookId;

        // 书籍类型
        @NonNull BookType bookType;

        // 图书馆id
        @NonNull UUID libraryBranchId;

        // 持有开始时间
        @NonNull Instant holdFrom;

        // 持有结束时间
        Instant holdTill;

        public static BookPlacedOnHold bookPlacedOnHoldNow(BookId bookId, BookType bookType, LibraryBranchId libraryBranchId, PatronId patronId, HoldDuration holdDuration) {
            return new BookPlacedOnHold(
                    Instant.now(),
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    bookType,
                    libraryBranchId.getLibraryBranchId(),
                    holdDuration.getFrom(),
                    holdDuration.getTo().getOrNull());
        }
    }


    //
    @Value
    class BookPlacedOnHoldEvents implements PatronEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull UUID patronId;
        @NonNull BookPlacedOnHold bookPlacedOnHold;
        @NonNull Option<MaximumNumberOhHoldsReached> maximumNumberOhHoldsReached;

        @Override
        public Instant getWhen() {
            return bookPlacedOnHold.when;
        }

        public static BookPlacedOnHoldEvents events(BookPlacedOnHold bookPlacedOnHold) {
            return new BookPlacedOnHoldEvents(bookPlacedOnHold.getPatronId(), bookPlacedOnHold, Option.none());
        }

        public static BookPlacedOnHoldEvents events(BookPlacedOnHold bookPlacedOnHold, MaximumNumberOhHoldsReached maximumNumberOhHoldsReached) {
            return new BookPlacedOnHoldEvents(bookPlacedOnHold.patronId, bookPlacedOnHold, Option.of(maximumNumberOhHoldsReached));
        }

        public List<DomainEvent> normalize() {
            return List.<DomainEvent>of(bookPlacedOnHold).appendAll(maximumNumberOhHoldsReached.toList());
        }
    }

    // 最大借阅数量到达事件
    @Value
    class MaximumNumberOhHoldsReached implements PatronEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;
        @NonNull UUID patronId;
        int numberOfHolds;

        public static MaximumNumberOhHoldsReached now(PatronInformation patronInformation, int numberOfHolds) {
            return new MaximumNumberOhHoldsReached(
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    numberOfHolds);
        }
    }

    @Value
    class BookCheckedOut implements PatronEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull BookType bookType;
        @NonNull UUID libraryBranchId;
        @NonNull Instant till;

        public static BookCheckedOut bookCheckedOutNow(BookId bookId, BookType bookType, LibraryBranchId libraryBranchId, PatronId patronId, CheckoutDuration checkoutDuration) {
            return new BookCheckedOut(
                    Instant.now(),
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    bookType,
                    libraryBranchId.getLibraryBranchId(),
                    checkoutDuration.to());
        }
    }


    // 书籍归还事件
    @Value
    class BookReturned implements PatronEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;


        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull BookType bookType;
        @NonNull UUID libraryBranchId;
    }

    @Value
    class BookHoldFailed implements PatronEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull String reason;
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull UUID libraryBranchId;

        static BookHoldFailed bookHoldFailedNow(Rejection rejection, BookId bookId, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new BookHoldFailed(
                    rejection.getReason().getReason(),
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class BookCheckingOutFailed implements PatronEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull String reason;
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull UUID libraryBranchId;

        static BookCheckingOutFailed bookCheckingOutFailedNow(Rejection rejection, BookId bookId, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new BookCheckingOutFailed(
                    rejection.getReason().getReason(),
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }


    // 借阅取消事件
    @Value
    class BookHoldCanceled implements PatronEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull UUID libraryBranchId;

        public static BookHoldCanceled holdCanceledNow(BookId bookId, LibraryBranchId libraryBranchId, PatronId patronId) {
            return new BookHoldCanceled(
                    Instant.now(),
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class BookHoldCancelingFailed implements PatronEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull UUID libraryBranchId;

        static BookHoldCancelingFailed holdCancelingFailedNow(BookId bookId, LibraryBranchId libraryBranchId, PatronId patronId) {
            return new BookHoldCancelingFailed(
                    Instant.now(),
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class BookHoldExpired implements PatronEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull UUID libraryBranchId;

        public static BookHoldExpired now(BookId bookId, PatronId patronId, LibraryBranchId libraryBranchId) {
            return new BookHoldExpired(
                    Instant.now(),
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }


    // 借阅人过期持有事件
    @Value
    class OverdueCheckoutRegistered implements PatronEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull UUID libraryBranchId;

        public static OverdueCheckoutRegistered now(PatronId patronId, BookId bookId, LibraryBranchId libraryBranchId) {
            return new OverdueCheckoutRegistered(
                    Instant.now(),
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

}



