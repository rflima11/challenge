import http from 'k6/http';
import { check } from 'k6'; // Removi o 'sleep'
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

// Função geradora de CPF (Manti igual, necessária para passar na validação)
function generateCPF(formatted = false) {
    const rnd = (n) => Math.round(Math.random() * n);
    const mod = (base, div) => Math.round(base - Math.floor(base / div) * div);

    const n1 = rnd(9);
    const n2 = rnd(9);
    const n3 = rnd(9);
    const n4 = rnd(9);
    const n5 = rnd(9);
    const n6 = rnd(9);
    const n7 = rnd(9);
    const n8 = rnd(9);
    const n9 = rnd(9);

    let d1 = n9 * 2 + n8 * 3 + n7 * 4 + n6 * 5 + n5 * 6 + n4 * 7 + n3 * 8 + n2 * 9 + n1 * 10;
    d1 = 11 - mod(d1, 11);
    if (d1 >= 10) d1 = 0;

    let d2 = d1 * 2 + n9 * 3 + n8 * 4 + n7 * 5 + n6 * 6 + n5 * 7 + n4 * 8 + n3 * 9 + n2 * 10 + n1 * 11;
    d2 = 11 - mod(d2, 11);
    if (d2 >= 10) d2 = 0;

    if (formatted) {
        return `${n1}${n2}${n3}.${n4}${n5}${n6}.${n7}${n8}${n9}-${d1}${d2}`;
    }
    return `${n1}${n2}${n3}${n4}${n5}${n6}${n7}${n8}${n9}${d1}${d2}`;
}

export const options = {
    // Desabilitei thresholds de falha para o teste não parar se o servidor explodir.
    // Queremos ver até onde aguenta.
    thresholds: {
        http_req_failed: ['rate<1.00'], // Aceita até 100% de erro (só pra gerar relatório)
    },

    // Configuração para ALTO VOLUME
    scenarios: {
        stress_test: {
            executor: 'constant-vus', // Mantém a carga constante e alta
            vus: 100,                 // 300 Threads simultâneas (Cuidado com CPU do PC)
            duration: '45s',          // 45 segundos de espancamento
        },
    },
    // Otimização de rede para não estourar as portas do seu PC
    discardResponseBodies: true, // Não precisamos ler o corpo da resposta (economiza RAM)
};

export default function () {
    const url = 'http://localhost:5000/v1/accounts';

    const randomCpf = generateCPF(false);

    const payload = JSON.stringify({
        nome: `User Stress`,
        cpf: randomCpf,
        dataNascimento: "1995-05-20",
        // FIX: Email único por request (usando o CPF randômico) para não dar Duplicate Key
        email: `u.${randomCpf}@stress.com`
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        timeout: '2s', // Se demorar mais que 2s, aborta e tenta o próximo
    };

    const res = http.post(url, payload, params);

    check(res, {
        'status is 202': (r) => r.status === 202,
    });

    // ZERO SLEEP. Loop infinito.
}

export function handleSummary(data) {
    return {
        "summary.html": htmlReport(data),
    };
}