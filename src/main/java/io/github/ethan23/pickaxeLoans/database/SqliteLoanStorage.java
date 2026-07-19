package io.github.ethan23.pickaxeLoans.database;

import io.github.ethan23.pickaxeLoans.model.CostType;
import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.model.LoanState;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqliteLoanStorage implements LoanStorage {

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS loans (
                loan_uuid          TEXT PRIMARY KEY,
                lender_uuid        TEXT NOT NULL,
                pickaxe            BLOB NOT NULL,
                state              TEXT NOT NULL,
                cost_type          TEXT NOT NULL,
                upfront_cost       REAL NOT NULL,
                xp_tax_percent     INTEGER NOT NULL,
                energy_tax_percent INTEGER NOT NULL,
                duration_millis    INTEGER NOT NULL,
                created_at         INTEGER NOT NULL,
                listing_expires_at INTEGER NOT NULL,
                borrower_uuid      TEXT,
                started_at         INTEGER,
                ends_at            INTEGER,
                xp_accrued         TEXT,
                energy_accrued     TEXT
            )""";

    private static final String UPSERT_SQL = """
            INSERT OR REPLACE INTO loans (
                loan_uuid, lender_uuid, pickaxe, state, cost_type, upfront_cost,
                xp_tax_percent, energy_tax_percent, duration_millis, created_at,
                listing_expires_at, borrower_uuid, started_at, ends_at, xp_accrued, energy_accrued
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";

    private final Path databasePath;
    private Connection connection;

    public SqliteLoanStorage(Path databasePath) {
        this.databasePath = databasePath;
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(databasePath.getParent());
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);

            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA journal_mode = WAL");
                statement.execute("PRAGMA synchronous = NORMAL");
            }

            createSchema();
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Could not open loan database at " + databasePath, e);
        }
    }

    private void createSchema() throws SQLException {
        int version;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA user_version")) {
            version = resultSet.getInt(1);
        }

        if (version < 1) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(CREATE_TABLE_SQL);
                statement.execute("PRAGMA user_version = 1");
            }
        }
    }

    @Override
    public void upsert(Loan loan) {
        LoanRecord loanRecord = loan.toRecord();

        try (PreparedStatement statement = connection.prepareStatement(UPSERT_SQL)) {
            statement.setString(1, loanRecord.loanUUID().toString());
            statement.setString(2, loanRecord.lenderUUID().toString());
            statement.setBytes(3, loanRecord.pickaxe());
            statement.setString(4, loanRecord.loanState().name());
            statement.setString(5, loanRecord.costType().name());
            statement.setDouble(6, loanRecord.upFrontCost());
            statement.setInt(7, loanRecord.xpTaxPercent());
            statement.setInt(8, loanRecord.energyTaxPercent());
            statement.setLong(9, loanRecord.loanDurationMillis());
            statement.setLong(10, loanRecord.createdAt());
            statement.setLong(11, loanRecord.listingExpiresAt());

            LoanRecord.ActiveLoanRecord activeLoanRecord = loanRecord.activeLoanRecord();
            if (activeLoanRecord == null) {
                for (int i = 12; i <= 16; i++) {
                    statement.setObject(i, null);
                }
            } else {
                statement.setString(12, activeLoanRecord.borrowerUUID().toString());
                statement.setLong(13, activeLoanRecord.startedAt());
                statement.setLong(14, activeLoanRecord.endsAt());
                statement.setString(15, activeLoanRecord.xpAccrued().toPlainString());
                statement.setString(16, activeLoanRecord.energyAccrued().toPlainString());
            }

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not save loan " + loanRecord.loanUUID(), e);
        }
    }

    @Override
    public void delete(UUID loanUUID) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM loans WHERE loan_uuid = ?")) {
            statement.setString(1, loanUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not delete loan " + loanUUID, e);
        }
    }

    @Override
    public List<Loan> loadAll() {
        List<Loan> loans = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM loans ORDER BY created_at");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                loans.add(Loan.fromRecord(readRecord(resultSet)));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load loans", e);
        }

        return loans;
    }

    private LoanRecord readRecord(ResultSet resultSet) throws SQLException {
        LoanRecord.ActiveLoanRecord activeLoanRecord = null;

        // getLong() on a NULL column returns 0, so "is this loan borrowed" must be
        // decided by a TEXT column, where NULL comes back as an honest null
        String borrowerUUID = resultSet.getString("borrower_uuid");
        if (borrowerUUID != null) {
            activeLoanRecord = new LoanRecord.ActiveLoanRecord(
                    UUID.fromString(borrowerUUID),
                    new BigDecimal(resultSet.getString("xp_accrued")),
                    new BigDecimal(resultSet.getString("energy_accrued")),
                    resultSet.getLong("started_at"),
                    resultSet.getLong("ends_at"));
        }

        return new LoanRecord(
                UUID.fromString(resultSet.getString("loan_uuid")),
                resultSet.getBytes("pickaxe"),
                UUID.fromString(resultSet.getString("lender_uuid")),
                CostType.valueOf(resultSet.getString("cost_type")),
                resultSet.getDouble("upfront_cost"),
                resultSet.getInt("xp_tax_percent"),
                resultSet.getInt("energy_tax_percent"),
                resultSet.getLong("duration_millis"),
                resultSet.getLong("created_at"),
                resultSet.getLong("listing_expires_at"),
                LoanState.valueOf(resultSet.getString("state")),
                activeLoanRecord);
    }

    @Override
    public void close() {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not close loan database", e);
        }
    }
}
