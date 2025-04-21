import { BrowserRouter, Routes, Route } from "react-router";
import Layout from "./pages/Layout";
import Login from "./pages/Test";

export default function App() {  
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Login />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}