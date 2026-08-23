import { Route, Routes } from "react-router-dom";
import Dashboard from "./pages/Dashboard";
import NewRisk from "./pages/NewRisk";
import RiskDetail from "./pages/RiskDetail";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Dashboard />} />
      <Route path="/risks/new" element={<NewRisk />} />
      <Route path="/risks/:id" element={<RiskDetail />} />
    </Routes>
  );
}
