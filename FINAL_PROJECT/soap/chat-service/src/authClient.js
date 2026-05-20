const soap = require('soap');

const AUTH_WSDL = process.env.AUTH_WSDL || 'http://localhost:9001/auth?wsdl';

let clientPromise = null;

function client() {
  if (!clientPromise) {
    clientPromise = soap.createClientAsync(AUTH_WSDL).catch(err => {
      clientPromise = null;
      throw err;
    });
  }
  return clientPromise;
}

async function validateToken(token) {
  const c = await client();
  const [resp] = await c.validateTokenAsync({ token });
  const info = resp && resp.return ? resp.return : resp;
  if (!info || info.userId === undefined) {
    const e = new Error('INVALID_TOKEN');
    e.code = 'INVALID_TOKEN';
    throw e;
  }
  return { userId: Number(info.userId), username: String(info.username) };
}

module.exports = { validateToken };
