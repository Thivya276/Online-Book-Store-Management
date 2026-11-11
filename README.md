# Marginalia Books (OnlineBookStore)

A Spring Boot and Thymeleaf online bookstore that stores everything in plain text files under `data/`. There is no database to set up.

## Run it
Requires Java 17+ and Maven.

    mvn spring-boot:run

Open http://localhost:8080 (run the command from the project root so the `data/` folder is found).

| Role | Log in at | Credentials |
|------|-----------|-------------|
| Customer | /login | demo@bookstore.com / demo123 (or sign up) |
| Admin | /admin/login | admin / admin123 |

## Features
- **Customers:** browse and search books, filter by genre, add to cart, place and cancel orders, track order status, write, edit and delete reviews, edit profile.
- **Admin:** dashboard with revenue and low-stock list, add/edit/delete books, manage users, update order status, moderate reviews.
- Stock is reduced when an order is placed and restored when it is cancelled.

## Structure
- `model/` plain classes that read and write one line of a text file each
- `service/` business rules and file access
- `controller/` web routes and session checks
- `util/` `FileHandler` (file I/O), `Validation`, `IdGenerator`
- `templates/` Thymeleaf pages, with shared parts in `fragments.html`
- `data/` pipe-delimited files: `books.txt`, `users.txt`, `orders.txt`, `admins.txt`, `reviews.txt`

## Security and payment
- **Passwords:** BCrypt. Older SHA-256 hashes still work and upgrade to BCrypt on the next login.
- **Lockout:** 5 wrong passwords locks that login for 10 minutes (customer and admin).
- **CSRF:** every POST must carry a session token (added by `common.js`, checked in `SecurityConfig`).
- **Sessions:** 15 minute idle timeout, http-only cookie, new session id on login, clickjacking and sniffing headers.
- **Admin password:** the dashboard warns until the default `admin123` is changed at `/admin/password`.
- **Payment:** cash on delivery works for real. UPI and Card are a demo and mark the order paid at once. See `PaymentService` to plug in Razorpay or Stripe.
- Still missing for a live site: HTTPS, a real database, and a payment webhook.

## Notes
- Passwords are stored as unsalted SHA-256 hashes, which is fine for a coursework demo. Use BCrypt (Spring Security) before any real deployment.
- Data files are read on every request. That keeps the code simple but suits small stores only.
- Change the default admin password by replacing the hash in `data/admins.txt` (delete the file to recreate admin / admin123).
