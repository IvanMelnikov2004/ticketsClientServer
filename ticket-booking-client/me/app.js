document.addEventListener("DOMContentLoaded", () => {
    const userDetails = document.getElementById("userDetails");
    const depositForm = document.getElementById("depositForm");
    const depositRequests = document.getElementById("depositRequests");

    const apiBaseUrl = "http://localhost:8080";

    // Проверяем наличие токенов
    const accessToken = localStorage.getItem("accessToken");
    const refreshToken = localStorage.getItem("refreshToken");

    if (!accessToken || !refreshToken) {
        alert("Вы не авторизованы!");
        window.location.href = "/login";
        return;
    }

    // Универсальная функция для запросов с токеном
    async function fetchWithToken(url, options = {}) {
        const currentAccessToken = localStorage.getItem("accessToken");
        options.headers = {
            ...options.headers,
            Authorization: `Bearer ${currentAccessToken}`
        };

        try {
            let response = await fetch(url, options);

            // Если токен истек (401 и код TOKEN_EXPIRED), обновляем токен
            if (response.status === 401) {
                const responseData = await response.json();
                if (responseData.code === "TOKEN_EXPIRED") {
                    console.warn("Токен истек. Выполняется обновление токена...");
                    const newTokens = await refreshAccessToken(refreshToken);

                    // Обновляем токены в localStorage
                    localStorage.setItem("accessToken", newTokens.accessToken);
                    localStorage.setItem("refreshToken", newTokens.refreshToken);

                    // Повторяем запрос с новым токеном
                    options.headers.Authorization = `Bearer ${newTokens.accessToken}`;
                    response = await fetch(url, options);
                }
            }

            // Проверяем, существует ли объект response и его заголовки
            if (!response || !response.headers) {
                throw new Error("Некорректный ответ от сервера");
            }

            // Проверяем, возвращает ли сервер JSON
            const contentType = response.headers.get("Content-Type") || "";
            if (contentType.includes("application/json")) {
                return await response.json();
            } else {
                // Если сервер возвращает текст
                const text = await response.text();
                return { message: text };
            }
        } catch (error) {
            console.error("Ошибка при выполнении запроса:", error.message);
            throw error;
        }
    }

    // Функция для обновления токенов
    async function refreshAccessToken(refreshToken) {
        try {
            const response = await fetch(`${apiBaseUrl}/auth/refresh`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ refreshToken })
            });

            if (!response.ok) {
                throw new Error("Ошибка обновления токена");
            }

            return await response.json();
        } catch (error) {
            console.error("Ошибка обновления токена:", error.message);
            alert("Сессия истекла. Пожалуйста, войдите заново.");
            localStorage.clear();
            window.location.href = "/auth";
            throw error;
        }
    }

    // Получение информации о пользователе
    async function fetchUserInfo() {
        try {
            const userInfo = await fetchWithToken(`${apiBaseUrl}/users/info`);
            userDetails.innerHTML = `
                <p>ID: ${userInfo.id}</p>
                <p>Email: ${userInfo.email}</p>
                <p>Имя: ${userInfo.firstname} ${userInfo.lastname}</p>
                <p>Дата рождения: ${userInfo.birthDate}</p>
                <p>Баланс: ${userInfo.balance}</p>
            `;
        } catch (error) {
            console.error("Ошибка получения информации о пользователе:", error);
        }
    }

    // Создание запроса на пополнение
    depositForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        
        const amount = parseInt(document.getElementById("amount").value, 10);

        if (isNaN(amount) || amount <= 0) {
            alert("Введите корректную сумму для пополнения.");
            return;
        }

        try {
            const response = await fetch(`${apiBaseUrl}/deposits/create`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${localStorage.getItem("accessToken")}`
                },
                body: JSON.stringify({ amount })
            });

            // Проверяем, существует ли объект response и его заголовки
            if (!response || !response.headers) {
                throw new Error("Некорректный ответ от сервера");
            }

            const contentType = response.headers.get("Content-Type") || "";
            if (contentType.includes("application/json")) {
                const data = await response.json();
                console.log("Сервер вернул JSON:", data);
            } else {
                const message = await response.text();
                console.log("Сервер вернул текст:", message);
            }

            alert("Запрос на пополнение создан!");
            fetchDeposits(); // Обновляем список депозитов
        } catch (error) {
            console.error("Ошибка создания запроса на пополнение:", error.message);
        }
    });

    // Получение списка запросов на пополнение
    async function fetchDeposits() {
        try {
            const deposits = await fetchWithToken(`${apiBaseUrl}/deposits/last`);
            depositRequests.innerHTML = deposits.map((deposit) => `
                <div class="deposit">
                    <p>ID: ${deposit.id}</p>
                    <p>Сумма: ${deposit.amount}</p>
                    <p>Статус: ${deposit.status}</p>
                    <p>Дата создания: ${deposit.createdAt}</p>
                    ${deposit.status === "pending" ? `
                        <button onclick="changeDepositStatus(${deposit.id}, 'completed')">Подтвердить</button>
                        <button onclick="changeDepositStatus(${deposit.id}, 'failed')">Отменить</button>
                    ` : ""}
                </div>
            `).join("");
        } catch (error) {
            console.error("Ошибка получения запросов на пополнение:", error);
        }
    }

    // Изменение статуса депозита
    window.changeDepositStatus = async (depositId, status) => {
        try {
            await fetchWithToken(`${apiBaseUrl}/deposits/confirm`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ depositId, status })
            });
            alert(`Статус депозита изменен на ${status}`);
            
            // Обновляем данные пользователя и список запросов
            fetchUserInfo(); 
            fetchDeposits(); 
        } catch (error) {
            console.error("Ошибка изменения статуса депозита:", error);
        }
    };

    // Инициализация личного кабинета
    fetchUserInfo();
    fetchDeposits();
});