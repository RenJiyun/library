package io.pillopl.library.lending.book.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.BookCheckedOut;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCanceled;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExpired;
import io.pillopl.library.lending.patron.model.PatronEvent.BookReturned;
import io.pillopl.library.lending.patron.model.PatronId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

/**
 * 已被借阅的书籍
 */
@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "bookInformation")
public class BookOnHold implements Book {

    @NonNull
    BookInformation bookInformation;

    @NonNull
    LibraryBranchId holdPlacedAt;

    @NonNull
    PatronId byPatron;

    @NonNull
    Instant holdTill;

    @NonNull
    Version version;

    public BookOnHold(BookId bookId, BookType type, LibraryBranchId libraryBranchId, PatronId patronId, Instant holdTill, Version version) {
        this(new BookInformation(bookId, type), libraryBranchId, patronId, holdTill, version);
    }


    /**
     * 处理书籍归还事件
     *
     * @param bookReturned
     * @return
     */
    public AvailableBook handle(BookReturned bookReturned) {
        return new AvailableBook(
                bookInformation, new LibraryBranchId(bookReturned.getLibraryBranchId()),
                version);
    }

    /**
     * 处理借阅过期事件
     *
     * @param bookHoldExpired
     * @return
     */
    public AvailableBook handle(BookHoldExpired bookHoldExpired) {
        return new AvailableBook(
                bookInformation,
                new LibraryBranchId(bookHoldExpired.getLibraryBranchId()),
                version);
    }


    /**
     * 处理书籍借阅提取事件
     *
     * @param bookCheckedOut
     * @return
     */
    public CheckedOutBook handle(BookCheckedOut bookCheckedOut) {
        return new CheckedOutBook(
                bookInformation,
                new LibraryBranchId(bookCheckedOut.getLibraryBranchId()),
                new PatronId(bookCheckedOut.getPatronId()),
                version);
    }


    /**
     * 处理借阅取消事件
     *
     * @param bookHoldCanceled
     * @return
     */
    public AvailableBook handle(BookHoldCanceled bookHoldCanceled) {
        return new AvailableBook(
                bookInformation, new LibraryBranchId(bookHoldCanceled.getLibraryBranchId()),
                version);
    }


    public BookId getBookId() {
        return bookInformation.getBookId();
    }

    public boolean by(PatronId patronId) {
        return byPatron.equals(patronId);
    }
}

