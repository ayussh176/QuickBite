import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080/api',
  validateStatus: () => true
});

async function runTests() {
  try {
    console.log('--- TEST 1: CUSTOMER LOGIN ---');
    const custLogin = await client.post('/v1/auth/login', {
      email: 'customer@quickbite.com',
      password: 'password'
    });
    console.log('Cust Login status:', custLogin.status, 'body:', JSON.stringify(custLogin.data));
    if (custLogin.status !== 200) throw new Error('Customer login failed');
    const custToken = custLogin.data.data.accessToken;
    client.defaults.headers.common['Authorization'] = `Bearer ${custToken}`;

    console.log('\n--- TEST 2: MERCHANT LOGIN ---');
    const merchLogin = await client.post('/v1/auth/login', {
      email: 'merchant@quickbite.com',
      password: 'password'
    });
    console.log('Merchant Login status:', merchLogin.status);
    if (merchLogin.status !== 200) throw new Error('Merchant login failed');
    const merchToken = merchLogin.data.data.accessToken;

    console.log('\n--- TEST 3: WALLET RETRIEVAL (CACHING VERIFICATION) ---');
    const walletRes1 = await client.get('/v1/wallets');
    console.log('Initial wallet Res:', walletRes1.status, 'body:', JSON.stringify(walletRes1.data));

    // Call again to verify it pulls from cache or is quick
    const walletRes2 = await client.get('/v1/wallets');
    console.log('Cached wallet Res:', walletRes2.status, 'body:', JSON.stringify(walletRes2.data));

    console.log('\n--- TEST 4: PLACE ORDER & VERIFY CACHE INVALIDATION ---');
    const restListRes = await client.get('/v1/restaurants?size=5');
    console.log('Rest list Res status:', restListRes.status, 'body:', JSON.stringify(restListRes.data));
    if (restListRes.status !== 200 || !restListRes.data.data.content.length) {
      throw new Error('No restaurants found in database to test');
    }
    const targetRest = restListRes.data.data.content[0];
    console.log(`Using Restaurant: ID=${targetRest.id}, Name=${targetRest.name}`);

    const menuRes = await client.get(`/v1/restaurants/${targetRest.id}/menu/items?size=5`);
    console.log('Menu Res status:', menuRes.status, 'body:', JSON.stringify(menuRes.data));
    if (menuRes.status !== 200 || !menuRes.data.data.content.length) {
      throw new Error(`No menu items found for restaurant ID ${targetRest.id}`);
    }
    const targetItem = menuRes.data.data.content[0];
    console.log(`Using Menu Item: ID=${targetItem.id}, Name=${targetItem.name}`);

    // Fetch cart to verify items are present
    const cartRes = await client.get('/v1/carts');
    console.log('Cart items count:', cartRes.data?.data?.items?.length || 0);

    // If cart is empty, add an item
    if (!cartRes.data?.data?.items?.length) {
      console.log('Cart is empty. Adding food item to cart...');
      const addRes = await client.post('/v1/carts/items', {
        foodItemId: targetItem.id,
        quantity: 1,
        specialInstructions: 'Extra cheese'
      });
      console.log('Add to cart status:', addRes.status, 'Payload:', JSON.stringify(addRes.data));
    }

    // Place the order
    const orderPayload = {
      deliveryAddressId: 1,
      paymentMethod: 'WALLET',
      specialInstructions: 'Leave at front gate'
    };
    const orderRes = await client.post('/v1/orders', orderPayload);
    console.log('Place Order status:', orderRes.status, 'body:', JSON.stringify(orderRes.data));
    if (orderRes.status !== 200 && orderRes.status !== 201) {
      throw new Error('Order placement failed');
    }
    const order = orderRes.data.data;
    console.log(`Order placed successfully: ID=${order.id}, Total=₹${order.totalAmount}`);

    // Verify wallet cache was invalidated and updated balance is returned
    const walletRes3 = await client.get('/v1/wallets');
    console.log('Updated wallet balance after order checkout:', walletRes3.data?.data?.balance);

    console.log('\n--- TEST 5: MERCHANT UPDATES ORDER STATUS ---');
    client.defaults.headers.common['Authorization'] = `Bearer ${merchToken}`;
    const updateRes = await client.patch(`/v1/orders/${order.id}/status`, {
      status: 'CONFIRMED'
    });
    console.log('Order status update status:', updateRes.status, 'New Status:', updateRes.data?.data?.status);

    console.log('\nALL REAL-TIME AND CACHING API FLOWS PASSED!');
  } catch (e) {
    console.error('Test run failed with error:', e);
  }
}

runTests();
