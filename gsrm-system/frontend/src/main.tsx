import { HashRouter } from 'react-router-dom';
// ... (其他 import 保持不變)

// ... (queryClient 保持不變)

ReactDOM.createRoot(rootElement).render(
  <React.StrictMode>
    <HashRouter>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <App />
        </AuthProvider>
      </QueryClientProvider>
    </HashRouter>
  </React.StrictMode>,
);
