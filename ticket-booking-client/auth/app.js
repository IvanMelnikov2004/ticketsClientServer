document.addEventListener("DOMContentLoaded", () => {
    const registerForm = document.getElementById("registerForm");
    const loginForm = document.getElementById("loginForm");
    const registerError = document.getElementById("registerError");
    const loginError = document.getElementById("loginError");
    if (!registerError || !loginError) {
        console.error('Элементы ошибок не найдены!');
      }


    function showError(element, message) {
        element.textContent = message;
        element.classList.add('show');
        setTimeout(() => element.classList.remove('show'), 5000);
    }

    function validateCredentials(email, password) {
        const errors = [];
        
        if (password.length < 8 || password.length > 20 || 
            !/[a-zA-Z]/.test(password) || 
            !/\d/.test(password) || 
            !/[!@#$%^&*]/.test(password)) {
            errors.push("Пароль должен содержать от 8 до 20 символов, хотя бы одну букву, цифру и спецсимвол");
        }
        
        if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) {
            errors.push("Некорректный email");
        }
        
        return errors;
    }

    async function fetchWithToken(url, options) {
        let response = await fetch(url, options);
        let data = await response.json();
        
        if (response.status === 401 && data.code === "TOKEN_EXPIRED") {
            const refreshResponse = await fetch("http://localhost:8080/auth/refresh", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ refreshToken: localStorage.getItem("refreshToken") })
            });
            
            const refreshData = await refreshResponse.json();
            if (refreshResponse.ok) {
                localStorage.setItem("accessToken", refreshData.accessToken);
                options.headers["Authorization"] = "Bearer " + refreshData.accessToken;
                response = await fetch(url, options);
                data = await response.json();
            }
        }
        return { response, data };
    }
    
    registerForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        registerError.classList.remove('show');
        
        const userData = {
            firstname: document.getElementById("firstname").value,
            lastname: document.getElementById("lastname").value,
            email: document.getElementById("email").value,
            password: document.getElementById("password").value,
            birthDate: document.getElementById("birthDate").value
        };
        
        const errors = validateCredentials(userData.email, userData.password);
        if (errors.length > 0) {
            showError(registerError, errors.join("\n"));
            return;
        }
        
        try {
            const { response, data } = await fetchWithToken("http://localhost:8080/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(userData)
            });
            
            if (response.ok) {
                localStorage.setItem("accessToken", data.accessToken);
                localStorage.setItem("refreshToken", data.refreshToken);
                window.location.href = "/";
            } else if (response.status === 409) {
                
                showError(registerError, "Пользователь с таким email уже зарегистрирован");
            } else {
                const errorMessage = response.status === 401 
                    ? "Неавторизованный доступ" 
                    : data.message?.join("\n") || "Ошибка регистрации";
                showError(registerError, errorMessage);
            }
        } catch (error) {
            showError(registerError, "Ошибка соединения с сервером");
        }
    });
    
    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        loginError.classList.remove('show');
        
        const loginData = {
            email: document.getElementById("loginEmail").value,
            password: document.getElementById("loginPassword").value
        };
        
        const errors = validateCredentials(loginData.email, loginData.password);
        if (errors.length > 0) {
            showError(loginError, errors.join("\n"));
            return;
        }
        
        try {
            const { response, data } = await fetchWithToken("http://localhost:8080/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(loginData)
            });
            
            if (response.ok) {
                localStorage.setItem("accessToken", data.accessToken);
                localStorage.setItem("refreshToken", data.refreshToken);
                window.location.href = "/";
            } else {
                const errorMessage = response.status === 401 
                    ? "Неверные учетные данные" 
                    : data.message?.join("\n") || "Ошибка авторизации";
                showError(loginError, errorMessage);
            }
        } catch (error) {
            showError(loginError, "Ошибка соединения с сервером");
        }
    });
});