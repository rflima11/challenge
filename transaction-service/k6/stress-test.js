import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

// --- CONFIGURAÇÕES DO TESTE ---
export const options = {
    scenarios: {
        stress_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 50 },  // Sobe para 50 usuários
                { duration: '1m', target: 50 },   // Mantém 50 usuários
                { duration: '30s', target: 0 },   // Desce para 0
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'],
    },
};

// --- MÉTRICAS PERSONALIZADAS ---
const countDeposits = new Counter('valid_deposits_count');
const countWithdrawals = new Counter('valid_withdrawals_count');

// --- DADOS DO TESTE ---
const ACCOUNT_ID = 'd290f1ee-6c54-4b01-90e6-d701748f0851';
const DEPOSIT_AMOUNT = 100.00;
const WITHDRAW_AMOUNT = 50.00;

function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

export default function () {
    const url = 'http://localhost:5001/v1/transactions';

    const isDeposit = Math.random() > 0.4;

    const operationType = isDeposit ? 'DEPOSIT' : 'WITHDRAWAL';
    const amount = isDeposit ? DEPOSIT_AMOUNT : WITHDRAW_AMOUNT;

    const idempotencyKey = generateUUID();

    const payload = JSON.stringify({
        accountId: ACCOUNT_ID,
        operationType: operationType,
        amount: amount
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Idempotency-Key': idempotencyKey,
        },
    };

    const res = http.post(url, payload, params);

    const success = check(res, {
        'status is 201': (r) => r.status === 201,
    });

    if (success) {
        if (isDeposit) {
            countDeposits.add(1);
        } else {
            countWithdrawals.add(1);
        }
    }

    sleep(0.01);
}