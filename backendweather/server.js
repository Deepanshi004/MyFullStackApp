const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');
const connectDB = require('./config/db');
const { googleLogin }= require('./controllers/authController')

dotenv.config();

const app = express();
app.use(express.json());
app.use(cors());


// Connect to MongoDB
connectDB();
app.post('/api/auth/google', googleLogin);

// Routes
const authRoutes = require('./routes/auth');
const bodyParser = require('body-parser');
app.use('/api/auth', authRoutes);
app.use(bodyParser.json())



// Start server
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
