const express = require("express");
const axios = require("axios");
const cors = require("cors");
const fs = require("fs");

const app = express();
app.use(express.json());
app.use(cors());

// ✅ Plaid API Credentials
const PLAID_CLIENT_ID = "67ad4f360245ff0021df53cc"; // Replace with your actual Plaid client ID
const PLAID_SECRET = "575c98acd3b5fb2924cd464840a5d1"; // Replace with your actual Plaid secret
const PLAID_ENV = "sandbox"; // Change to 'development' or 'production' if needed"

const ACCESS_TOKEN_FILE = "access_token.json";

// ✅ Load access token from file (if exists)
let storedAccessToken = null;
if (fs.existsSync(ACCESS_TOKEN_FILE)) {
    storedAccessToken = JSON.parse(fs.readFileSync(ACCESS_TOKEN_FILE, "utf8")).access_token;
    console.log("🔄 Loaded Access Token:", storedAccessToken);
}

// ✅ Route to exchange public_token for access_token
app.post("/exchange_public_token", async (req, res) => {
    try {
        const { public_token } = req.body;

        const response = await axios.post(`https://${PLAID_ENV}.plaid.com/item/public_token/exchange`, {
            client_id: PLAID_CLIENT_ID,
            secret: PLAID_SECRET,
            public_token
        });

        storedAccessToken = response.data.access_token;

        // ✅ Save access token to file
        fs.writeFileSync(ACCESS_TOKEN_FILE, JSON.stringify({ access_token: storedAccessToken }), "utf8");

        console.log("✅ Access Token Saved:", storedAccessToken);
        res.json({ access_token: storedAccessToken });
    } catch (error) {
        console.error("❌ Error exchanging token:", error.response?.data || error.message);
        res.status(500).json({ error: "Failed to exchange token" });
    }
});

// ✅ Route to fetch transactions
app.get("/api/transactions", async (req, res) => {
    try {
        if (!storedAccessToken) {
            return res.status(400).json({ error: "No access token available. Link an account first." });
        }

        const response = await axios.post(`https://${PLAID_ENV}.plaid.com/transactions/get`, {
            client_id: PLAID_CLIENT_ID,
            secret: PLAID_SECRET,
            access_token: storedAccessToken,
            start_date: "1900-01-01",
            end_date: "2025-12-31"
        });

        console.log("Raw Transactions Response:", JSON.stringify(response.data, null, 2));
        console.log("Transactions Retrieved");
        res.json(response.data);
    } catch (error) {
        console.error("Error fetching transactions:", error.response?.data || error.message);
        res.status(500).json({ error: "Failed to fetch transactions" });
    }
});

app.post("/transactions/refresh", async (req, res) => {
    try {
        if (!storedAccessToken) {
            return res.status(400).json({ error: "No access token available. Link an account first." });
        }

        const response = await axios.post(`https://${PLAID_ENV}.plaid.com/transactions/refresh`, {
            client_id: PLAID_CLIENT_ID,
            secret: PLAID_SECRET,
            access_token: storedAccessToken
        });

        console.log("Transactions Refreshed");
        res.json({ message: "Transactions refreshed successfully!", request_id: response.data.request_id });
    } catch (error) {
        console.error("Error refreshing transactions:", error.response?.data || error.message);
        res.status(500).json({ error: "Failed to refresh transactions" });
    }
});

// Start server
const PORT = 3000;
app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on http://0.0.0.0:${PORT}`);
});

module.exports = app;
