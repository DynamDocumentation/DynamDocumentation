import { BrowserRouter, Routes, Route } from "react-router";
import Layout from "./pages/Layout";
import Welcome from "./pages/Welcome";
import Details from "./pages/Details";

export default function App() {  
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Welcome />} />
          <Route path=":namespace" element={<Details />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}