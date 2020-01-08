package io.pillopl.library.lending.patron.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import lombok.NonNull;
import lombok.Value;

/**
 * 借阅人的一个书籍持有
 */
@Value
class Hold {

    @NonNull BookId bookId;
    @NonNull LibraryBranchId libraryBranchId;

}
