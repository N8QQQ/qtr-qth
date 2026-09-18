# Functional Programming Rules

Apply these rules strictly based on the language of the file being reviewed:

## Java (qtr-qth)
- **Immutability:** All domain models and DTOs must be immutable (e.g., `final` fields, Java 21 Records).
- **Monadic Containers:** Enforce the use of `io.vavr.control.Try` or `Option` for error handling and nullable returns.
- **No Exceptions for Flow Control:** Flag standard `try-catch` blocks if they are being used for domain logic branching instead of being wrapped at the system boundary.
- **Pure Functions:** Domain logic must not rely on hidden state mutations.

## TypeScript / JavaScript
- Enforce the use of `fp-ts` or native `Result` types (if configured).
- Reject hidden side-effects in data transformation pipelines.

## Python
- Enforce standard `Result` typing or robust `Try/Except` wrapping at the boundaries.
- Favor list comprehensions and declarative pipelines over imperative `for` loops with mutable state accumulators.
