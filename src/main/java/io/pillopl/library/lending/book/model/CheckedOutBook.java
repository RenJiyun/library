package io.pillopl.library.lending.book.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent;
import io.pillopl.library.lending.patron.model.PatronId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

/**
 * 已被借阅提取的书籍
 */
@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "bookInformation")
public class CheckedOutBook implements Book {

    @NonNull
    BookInformation bookInformation;

    @NonNull
    LibraryBranchId checkedOutAt;

    @NonNull
    PatronId byPatron;

    @NonNull
    Version version;

    public CheckedOutBook(BookId bookId, BookType type, LibraryBranchId libraryBranchId, PatronId patronId, Version version) {
        this(new BookInformation(bookId, type), libraryBranchId, patronId, version);
    }

    public BookId getBookId() {
        return bookInformation.getBookId();
    }


    /**
     * 处理书籍归还事件
     *
     * @param bookReturnedByPatron
     * @return
     */
    public AvailableBook handle(PatronEvent.BookReturned bookReturnedByPatron) {
        return new AvailableBook(
                bookInformation,
                new LibraryBranchId(bookReturnedByPatron.getLibraryBranchId()),
                version);
    }


}

