// Simple webhook receiver for testing MicroProfile OpenAPI webhooks
// Demonstrates webhook signature verification
//
// ⚠️  IMPORTANT: This is a TEST/DEVELOPMENT tool, NOT for production use.
// Production webhook receivers should implement:
//   - Rate limiting (e.g., using express-rate-limit)
//   - IP whitelisting
//   - Request size limits
//   - Proper error handling and monitoring
//   - See WEBHOOK_SECURITY_BEST_PRACTICES.adoc for details
//
// Usage:
//   1. Install dependencies: npm install express body-parser crypto
//   2. Run: node webhook-receiver.js
//   3. Use ngrok to expose: ngrok http 3000
//   4. Subscribe using the ngrok HTTPS URL

const express = require('express');
const bodyParser = require('body-parser');
const crypto = require('crypto');

const app = express();
const PORT = 3000;

// Your webhook secret (received when creating subscription)
let WEBHOOK_SECRET = process.env.WEBHOOK_SECRET || '';

// Middleware to capture raw body for signature verification
app.use(bodyParser.json({
  verify: (req, res, buf) => {
    req.rawBody = buf.toString('utf8');
  }
}));

// Verify webhook signature
function verifySignature(payload, signature, secret) {
  if (!secret) {
    console.warn('⚠️  No webhook secret configured - skipping verification');
    return true;
  }
  
  const hmac = crypto.createHmac('sha256', secret);
  const expectedSignature = hmac.update(payload).digest('base64');
  return crypto.timingSafeEqual(
    Buffer.from(signature),
    Buffer.from(expectedSignature)
  );
}

// Webhook endpoint
app.post('/webhooks/products', (req, res) => {
  console.log('\n' + '='.repeat(60));
  console.log('📨 Webhook Received!');
  console.log('='.repeat(60));
  
  const signature = req.headers['x-webhook-signature'];
  const eventType = req.headers['x-event-type'];
  const eventId = req.headers['x-event-id'];
  
  console.log('\n📋 Headers:');
  console.log('  X-Event-Type:', eventType);
  console.log('  X-Event-Id:', eventId);
  console.log('  X-Webhook-Signature:', signature);
  console.log('  Content-Type:', req.headers['content-type']);
  
  // Verify signature
  if (signature && WEBHOOK_SECRET) {
    const isValid = verifySignature(req.rawBody, signature, WEBHOOK_SECRET);
    
    if (!isValid) {
      console.log('\n❌ Invalid signature - rejecting webhook');
      return res.status(401).json({ error: 'Invalid signature' });
    }
    console.log('\n✅ Signature verified successfully');
  }
  
  // Process the event
  console.log('\n📦 Payload:');
  console.log(JSON.stringify(req.body, null, 2));
  
  const event = req.body;
  console.log('\n🔔 Event Details:');
  console.log(`  Event Type: ${event.eventType}`);
  console.log(`  Event ID: ${event.eventId}`);
  console.log(`  Timestamp: ${event.timestamp}`);
  
  if (event.product) {
    console.log('\n  📦 Product:');
    console.log(`    ID: ${event.product.id}`);
    console.log(`    Name: ${event.product.name}`);
    console.log(`    Price: $${event.product.price}`);
    console.log(`    SKU: ${event.product.sku}`);
    console.log(`    Category: ${event.product.category}`);
    console.log(`    Stock: ${event.product.stockQuantity}`);
  }
  
  console.log('\n' + '='.repeat(60));
  
  // Acknowledge receipt
  res.status(200).json({
    received: true,
    eventId: eventId,
    processedAt: new Date().toISOString()
  });
});

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    webhookSecretConfigured: !!WEBHOOK_SECRET,
    timestamp: new Date().toISOString()
  });
});

// Set webhook secret endpoint (for testing)
app.post('/set-secret', (req, res) => {
  WEBHOOK_SECRET = req.body.secret;
  console.log('✅ Webhook secret updated');
  res.json({ message: 'Secret updated successfully' });
});

app.listen(PORT, () => {
  console.log('\n' + '='.repeat(60));
  console.log('🎣 Webhook Receiver Started');
  console.log('='.repeat(60));
  console.log(`\n✅ Server listening on http://localhost:${PORT}`);
  console.log(`\n📍 Webhook endpoint: http://localhost:${PORT}/webhooks/products`);
  console.log(`\n🔐 Secret configured: ${WEBHOOK_SECRET ? 'Yes ✅' : 'No ⚠️  (verification disabled)'}`);
  
  if (!WEBHOOK_SECRET) {
    console.log('\n💡 To enable signature verification:');
    console.log('   1. Set WEBHOOK_SECRET environment variable, or');
    console.log('   2. POST to /set-secret with {"secret": "your-secret"}');
  }
  
  console.log('\n🌐 To expose publicly (for real webhooks):');
  console.log('   1. Install ngrok: https://ngrok.com/download');
  console.log('   2. Run: ngrok http 3000');
  console.log('   3. Use the HTTPS URL in your webhook subscription');
  
  console.log('\n📖 Example subscription:');
  console.log(`
curl -X POST http://localhost:5050/mp-ecomm-store/api/webhooks \\
  -H "Content-Type: application/json" \\
  -d '{
    "url": "https://your-ngrok-url.ngrok.io/webhooks/products",
    "events": ["product.created", "product.updated"],
    "description": "Local webhook testing"
  }'
  `);
  
  console.log('='.repeat(60) + '\n');
});
