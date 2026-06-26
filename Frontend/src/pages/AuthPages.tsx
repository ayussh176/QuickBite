import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useDispatch } from "react-redux";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { setCredentials } from "../redux/authSlice";
import { apiService } from "../services/api/apiClient";
import toast from "react-hot-toast";

// ==========================================
// ZOD VALIDATION SCHEMAS
// ==========================================

const loginSchema = z.object({
  role: z.enum(["CUSTOMER", "RESTAURANT", "DELIVERY", "ADMIN"]),
  email: z.string().min(1, "Email is required").email("Invalid email format"),
  password: z.string().min(6, "Password must be at least 6 characters")
});

const registerSchema = z.object({
  role: z.enum(["CUSTOMER", "RESTAURANT", "DELIVERY"]),
  name: z.string().min(2, "Name must be at least 2 characters"),
  email: z.string().min(1, "Email is required").email("Invalid email format"),
  password: z.string().min(8, "Password must be at least 8 characters"),
  phone: z.string().optional(),
  address: z.string().optional(),
  restaurantName: z.string().optional(),
  licenseNumber: z.string().optional(),
  cuisine: z.string().optional(),
  vehicleType: z.string().optional(),
  vehicleNumber: z.string().optional(),
}).superRefine((data, ctx) => {
  if (data.role === "CUSTOMER") {
    if (!data.phone || data.phone.trim().length < 10) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Valid phone number is required (min 10 digits)",
        path: ["phone"]
      });
    }
    if (!data.address || data.address.trim().length < 5) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Address is required",
        path: ["address"]
      });
    }
  } else if (data.role === "RESTAURANT") {
    if (!data.phone || data.phone.trim().length < 10) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Valid phone number is required (min 10 digits)",
        path: ["phone"]
      });
    }
    if (!data.restaurantName || data.restaurantName.trim().length < 3) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Restaurant Name must be at least 3 characters",
        path: ["restaurantName"]
      });
    }
    if (!data.address || data.address.trim().length < 5) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Restaurant Address is required",
        path: ["address"]
      });
    }
    if (!data.licenseNumber || data.licenseNumber.trim().length < 5) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Valid FSSAI License Number is required",
        path: ["licenseNumber"]
      });
    }
  } else if (data.role === "DELIVERY") {
    if (!data.phone || data.phone.trim().length < 10) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Valid phone number is required (min 10 digits)",
        path: ["phone"]
      });
    }
    if (!data.vehicleType || data.vehicleType.trim().length < 2) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Vehicle type is required",
        path: ["vehicleType"]
      });
    }
    if (!data.vehicleNumber || data.vehicleNumber.trim().length < 4) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Vehicle registration plate is required",
        path: ["vehicleNumber"]
      });
    }
  }
});

const forgotSchema = z.object({
  email: z.string().min(1, "Email is required").email("Invalid email format")
});

const otpSchema = z.object({
  otp: z.string().length(4, "OTP must be exactly 4 digits").regex(/^\d+$/, "OTP must contain only numbers")
});

const resetSchema = z.object({
  password: z.string().min(8, "Password must be at least 8 characters"),
  confirm: z.string().min(8, "Confirm Password is required")
}).refine((data) => data.password === data.confirm, {
  message: "Passwords do not match",
  path: ["confirm"]
});

type LoginFormValues = z.infer<typeof loginSchema>;
type RegisterFormValues = z.infer<typeof registerSchema>;
type ForgotFormValues = z.infer<typeof forgotSchema>;
type OtpFormValues = z.infer<typeof otpSchema>;
type ResetFormValues = z.infer<typeof resetSchema>;


// 1. WELCOME SCREEN
export const Welcome: React.FC = () => {
  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center px-4 py-12 relative overflow-hidden">
      {/* Visual Background Glow */}
      <div className="absolute top-[-20%] left-[-10%] w-[600px] h-[600px] bg-primary-container/10 rounded-full blur-3xl" />
      <div className="absolute bottom-[-20%] right-[-10%] w-[600px] h-[600px] bg-tertiary-container/10 rounded-full blur-3xl" />

      <div className="z-10 text-center max-w-lg space-y-6">
        <h1 className="text-display-lg font-bold text-primary-container tracking-tight">QuickBite</h1>
        <h2 className="text-2xl font-semibold text-on-surface">Hungry? We've got you covered.</h2>
        <p className="text-secondary text-body-lg">
          The ultimate delivery network connecting food lovers with top merchants, lightning-fast courier partners, and robust admin operations.
        </p>

        <div className="flex flex-col gap-3 max-w-xs mx-auto">
          <Link
            to="/auth/login"
            className="w-full bg-primary-container text-white py-3 rounded-full font-button text-center hover:bg-primary transition-all shadow-lg shadow-primary-container/20 font-bold"
          >
            Login to Account
          </Link>
          <Link
            to="/auth/register"
            className="w-full bg-white border border-outline-variant text-on-surface py-3 rounded-full font-button text-center hover:bg-surface-container-low transition-all font-semibold"
          >
            Join QuickBite
          </Link>
        </div>

        <div className="pt-8 grid grid-cols-3 gap-4 text-xs font-semibold text-secondary border-t border-outline-variant">
          <div>
            <span className="material-symbols-outlined text-primary-container block mb-1">delivery_dining</span>
            Fast Delivery
          </div>
          <div>
            <span className="material-symbols-outlined text-primary-container block mb-1">payments</span>
            Secure Payments
          </div>
          <div>
            <span className="material-symbols-outlined text-primary-container block mb-1">verified</span>
            100% Verified
          </div>
        </div>
      </div>
    </div>
  );
};

// 2. LOGIN SCREEN
export const Login: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors, isSubmitting }
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      role: "CUSTOMER",
      email: "",
      password: ""
    }
  });

  const selectedRole = watch("role");

  const onSubmit = async (data: LoginFormValues) => {
    try {
      const user = await apiService.login(data.email, data.role);
      dispatch(setCredentials({ user, token: "mock-token-123" }));
      toast.success(`Logged in as ${user.name}`);
      
      // Redirect based on role
      if (data.role === "CUSTOMER") navigate("/customer/home");
      else if (data.role === "RESTAURANT") navigate("/merchant/dashboard");
      else if (data.role === "DELIVERY") navigate("/delivery/dashboard");
      else if (data.role === "ADMIN") navigate("/admin/dashboard");
      else navigate("/customer/home");
    } catch {
      toast.error("Invalid credentials");
    }
  };

  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center px-4 py-12">
      <div className="w-full max-w-md bg-surface-container-lowest border border-outline-variant rounded-2xl p-8 shadow-xl relative">
        <h2 className="text-headline-lg font-bold text-on-surface mb-2">Welcome Back</h2>
        <p className="text-secondary mb-6">Enter your details to sign in to QuickBite</p>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-semibold mb-1 text-on-surface">Select Portal Role</label>
            <div className="grid grid-cols-4 gap-1.5">
              {(["CUSTOMER", "RESTAURANT", "DELIVERY", "ADMIN"] as const).map((r) => (
                <button
                  key={r}
                  type="button"
                  onClick={() => setValue("role", r)}
                  className={`py-2 px-1 text-[10px] font-bold rounded-xl border transition-all ${
                    selectedRole === r
                      ? "border-primary-container bg-primary-container/10 text-primary-container"
                      : "border-outline-variant hover:bg-surface-container-low text-secondary"
                  }`}
                >
                  {r}
                </button>
              ))}
            </div>
            {errors.role && (
              <span className="text-red-500 text-xs mt-1 block">{errors.role.message}</span>
            )}
          </div>

          <div>
            <label className="block text-sm font-semibold mb-1 text-on-surface">Email Address</label>
            <input
              type="email"
              placeholder="e.g. ayush@gmail.com"
              {...register("email")}
              className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                errors.email ? "border-red-500" : "border-outline-variant"
              }`}
            />
            {errors.email && (
              <span className="text-red-500 text-xs mt-1 block">{errors.email.message}</span>
            )}
          </div>

          <div>
            <div className="flex justify-between items-center mb-1">
              <label className="text-sm font-semibold text-on-surface">Password</label>
              <Link to="/auth/forgot-password" className="text-xs font-semibold text-primary-container hover:underline">
                Forgot?
              </Link>
            </div>
            <input
              type="password"
              placeholder="Enter password"
              {...register("password")}
              className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                errors.password ? "border-red-500" : "border-outline-variant"
              }`}
            />
            {errors.password && (
              <span className="text-red-500 text-xs mt-1 block">{errors.password.message}</span>
            )}
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-primary-container text-white py-3 rounded-full font-button hover:bg-primary transition-all font-bold disabled:opacity-50"
          >
            {isSubmitting ? "Signing in..." : "Sign In"}
          </button>
        </form>

        <p className="text-center text-sm text-secondary mt-6">
          Don't have an account?{" "}
          <Link to="/auth/register" className="font-semibold text-primary-container hover:underline">
            Register Here
          </Link>
        </p>
      </div>
    </div>
  );
};

// 3. JOIN SCREEN (Register)
export const Join: React.FC = () => {
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors, isSubmitting }
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      role: "CUSTOMER",
      name: "",
      email: "",
      password: "",
      phone: "",
      address: "",
      restaurantName: "",
      licenseNumber: "",
      cuisine: "Burgers",
      vehicleType: "Motorcycle",
      vehicleNumber: ""
    }
  });

  const selectedRole = watch("role");

  const onSubmit = async (data: RegisterFormValues) => {
    console.log("Register data:", data);
    await new Promise((resolve) => setTimeout(resolve, 500));
    
    if (data.role === "RESTAURANT") {
      const requests = JSON.parse(localStorage.getItem("qb_onboarding") || "[]");
      requests.push({
        id: `REQ-${Math.floor(1000 + Math.random() * 9000)}`,
        restaurantName: data.restaurantName || "New Restaurant",
        ownerName: data.name,
        email: data.email,
        phone: data.phone || "+91 99999 99999",
        cuisine: [data.cuisine || "Burgers"],
        address: data.address || "Main Street, City",
        submittedAt: new Date().toISOString().substring(0, 10),
        status: "pending",
        licenseNumber: data.licenseNumber || "FSSAI-PENDING"
      });
      localStorage.setItem("qb_onboarding", JSON.stringify(requests));
      toast.success("Merchant application submitted! Pending Admin Approval.");
    } else {
      toast.success("Account created successfully! Please verify email.");
    }
    
    navigate("/auth/verify-email");
  };

  const nameLabel = selectedRole === "RESTAURANT" ? "Owner Full Name" : selectedRole === "DELIVERY" ? "Rider Full Name" : "Full Name";

  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center px-4 py-12">
      <div className="w-full max-w-md bg-surface-container-lowest border border-outline-variant rounded-2xl p-8 shadow-xl">
        <h2 className="text-headline-lg font-bold text-on-surface mb-2">Create Account</h2>
        <p className="text-secondary mb-6">Join the QuickBite food delivery network</p>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-semibold mb-1 text-on-surface">Select Portal Role</label>
            <div className="grid grid-cols-3 gap-2">
              {(["CUSTOMER", "RESTAURANT", "DELIVERY"] as const).map((r) => (
                <button
                  key={r}
                  type="button"
                  onClick={() => setValue("role", r)}
                  className={`py-2 px-3 text-[11px] font-bold rounded-xl border transition-all ${
                    selectedRole === r
                      ? "border-primary-container bg-primary-container/10 text-primary-container"
                      : "border-outline-variant hover:bg-surface-container-low text-secondary"
                  }`}
                >
                  {r}
                </button>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-sm font-semibold mb-1 text-on-surface">{nameLabel}</label>
            <input
              type="text"
              placeholder="e.g. Ayush Malik"
              {...register("name")}
              className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                errors.name ? "border-red-500" : "border-outline-variant"
              }`}
            />
            {errors.name && (
              <span className="text-red-500 text-xs mt-1 block">{errors.name.message}</span>
            )}
          </div>

          <div>
            <label className="block text-sm font-semibold mb-1 text-on-surface">Email Address</label>
            <input
              type="email"
              placeholder="e.g. ayush@gmail.com"
              {...register("email")}
              className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                errors.email ? "border-red-500" : "border-outline-variant"
              }`}
            />
            {errors.email && (
              <span className="text-red-500 text-xs mt-1 block">{errors.email.message}</span>
            )}
          </div>

          <div>
            <label className="block text-sm font-semibold mb-1 text-on-surface">Password</label>
            <input
              type="password"
              placeholder="Minimum 8 characters"
              {...register("password")}
              className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                errors.password ? "border-red-500" : "border-outline-variant"
              }`}
            />
            {errors.password && (
              <span className="text-red-500 text-xs mt-1 block">{errors.password.message}</span>
            )}
          </div>

          {/* Conditional Role-Specific Fields */}
          {selectedRole === "CUSTOMER" && (
            <>
              <div>
                <label className="block text-sm font-semibold mb-1 text-on-surface">Phone Number</label>
                <input
                  type="text"
                  placeholder="e.g. +91 99999 88888"
                  {...register("phone")}
                  className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                    errors.phone ? "border-red-500" : "border-outline-variant"
                  }`}
                />
                {errors.phone && (
                  <span className="text-red-500 text-xs mt-1 block">{errors.phone.message}</span>
                )}
              </div>

              <div>
                <label className="block text-sm font-semibold mb-1 text-on-surface">Delivery Address</label>
                <input
                  type="text"
                  placeholder="e.g. H-15, Sector 44, Noida"
                  {...register("address")}
                  className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                    errors.address ? "border-red-500" : "border-outline-variant"
                  }`}
                />
                {errors.address && (
                  <span className="text-red-500 text-xs mt-1 block">{errors.address.message}</span>
                )}
              </div>
            </>
          )}

          {selectedRole === "RESTAURANT" && (
            <>
              <div>
                <label className="block text-sm font-semibold mb-1 text-on-surface">Restaurant Name</label>
                <input
                  type="text"
                  placeholder="e.g. The Pizza Place"
                  {...register("restaurantName")}
                  className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                    errors.restaurantName ? "border-red-500" : "border-outline-variant"
                  }`}
                />
                {errors.restaurantName && (
                  <span className="text-red-500 text-xs mt-1 block">{errors.restaurantName.message}</span>
                )}
              </div>

              <div>
                <label className="block text-sm font-semibold mb-1 text-on-surface">Phone Number</label>
                <input
                  type="text"
                  placeholder="e.g. +91 99999 88888"
                  {...register("phone")}
                  className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                    errors.phone ? "border-red-500" : "border-outline-variant"
                  }`}
                />
                {errors.phone && (
                  <span className="text-red-500 text-xs mt-1 block">{errors.phone.message}</span>
                )}
              </div>

              <div>
                <label className="block text-sm font-semibold mb-1 text-on-surface">Restaurant Address</label>
                <input
                  type="text"
                  placeholder="e.g. Shop 4, Sector 62, Noida"
                  {...register("address")}
                  className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                    errors.address ? "border-red-500" : "border-outline-variant"
                  }`}
                />
                {errors.address && (
                  <span className="text-red-500 text-xs mt-1 block">{errors.address.message}</span>
                )}
              </div>

              <div>
                <label className="block text-sm font-semibold mb-1 text-on-surface">FSSAI License Number</label>
                <input
                  type="text"
                  placeholder="14-digit FSSAI number"
                  {...register("licenseNumber")}
                  className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                    errors.licenseNumber ? "border-red-500" : "border-outline-variant"
                  }`}
                />
                {errors.licenseNumber && (
                  <span className="text-red-500 text-xs mt-1 block">{errors.licenseNumber.message}</span>
                )}
              </div>

              <div>
                <label className="block text-sm font-semibold mb-1 text-on-surface">Primary Cuisine</label>
                <select
                  {...register("cuisine")}
                  className="w-full px-4 py-2 border border-outline-variant rounded-xl bg-white focus:outline-none focus:border-primary-container"
                >
                  <option value="Burgers">Burgers</option>
                  <option value="Pizza">Pizza</option>
                  <option value="Sushi">Sushi</option>
                  <option value="Desserts">Desserts</option>
                  <option value="Biryani">Biryani</option>
                  <option value="Healthy">Healthy</option>
                </select>
              </div>
            </>
          )}

          {selectedRole === "DELIVERY" && (
            <>
              <div>
                <label className="block text-sm font-semibold mb-1 text-on-surface">Phone Number</label>
                <input
                  type="text"
                  placeholder="e.g. +91 99999 88888"
                  {...register("phone")}
                  className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                    errors.phone ? "border-red-500" : "border-outline-variant"
                  }`}
                />
                {errors.phone && (
                  <span className="text-red-500 text-xs mt-1 block">{errors.phone.message}</span>
                )}
              </div>

              <div>
                <label className="block text-sm font-semibold mb-1 text-on-surface">Vehicle Type</label>
                <select
                  {...register("vehicleType")}
                  className="w-full px-4 py-2 border border-outline-variant rounded-xl bg-white focus:outline-none focus:border-primary-container"
                >
                  <option value="Bicycle">Bicycle 🚲</option>
                  <option value="Motorcycle">Motorcycle 🏍️</option>
                  <option value="Car">Car 🚗</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-semibold mb-1 text-on-surface">Vehicle License Plate</label>
                <input
                  type="text"
                  placeholder="e.g. DL3C-AB-5678"
                  {...register("vehicleNumber")}
                  className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                    errors.vehicleNumber ? "border-red-500" : "border-outline-variant"
                  }`}
                />
                {errors.vehicleNumber && (
                  <span className="text-red-500 text-xs mt-1 block">{errors.vehicleNumber.message}</span>
                )}
              </div>
            </>
          )}

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-primary-container text-white py-3 rounded-full font-button hover:bg-primary transition-all font-bold disabled:opacity-50"
          >
            {isSubmitting ? "Creating Account..." : "Create Account"}
          </button>
        </form>

        <p className="text-center text-sm text-secondary mt-6">
          Already have an account?{" "}
          <Link to="/auth/login" className="font-semibold text-primary-container hover:underline">
            Login
          </Link>
        </p>
      </div>
    </div>
  );
};

// 4. FORGOT PASSWORD
export const ForgotPassword: React.FC = () => {
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm<ForgotFormValues>({
    resolver: zodResolver(forgotSchema),
    defaultValues: {
      email: ""
    }
  });

  const onSubmit = async (data: ForgotFormValues) => {
    console.log("Forgot password email:", data);
    await new Promise((resolve) => setTimeout(resolve, 300));
    toast.success("Password reset OTP sent to email");
    navigate("/auth/verify-otp");
  };

  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center px-4 py-12">
      <div className="w-full max-w-md bg-surface-container-lowest border border-outline-variant rounded-2xl p-8 shadow-xl">
        <h2 className="text-headline-lg font-bold text-on-surface mb-2">Forgot Password</h2>
        <p className="text-secondary mb-6">Enter your registered email to request password reset OTP.</p>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-semibold mb-1 text-on-surface">Email Address</label>
            <input
              type="email"
              placeholder="e.g. ayush@gmail.com"
              {...register("email")}
              className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                errors.email ? "border-red-500" : "border-outline-variant"
              }`}
            />
            {errors.email && (
              <span className="text-red-500 text-xs mt-1 block">{errors.email.message}</span>
            )}
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-primary-container text-white py-3 rounded-full font-button hover:bg-primary transition-all font-bold disabled:opacity-50"
          >
            {isSubmitting ? "Sending OTP..." : "Send OTP"}
          </button>
        </form>

        <div className="text-center mt-6">
          <Link to="/auth/login" className="text-sm font-semibold text-primary-container hover:underline">
            Back to Login
          </Link>
        </div>
      </div>
    </div>
  );
};

// 5. VERIFY OTP
export const VerifyOTP: React.FC = () => {
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm<OtpFormValues>({
    resolver: zodResolver(otpSchema),
    defaultValues: {
      otp: ""
    }
  });

  const onSubmit = async (data: OtpFormValues) => {
    console.log("OTP code:", data);
    await new Promise((resolve) => setTimeout(resolve, 300));
    toast.success("OTP Verified! Set your new password.");
    navigate("/auth/reset-password");
  };

  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center px-4 py-12">
      <div className="w-full max-w-md bg-surface-container-lowest border border-outline-variant rounded-2xl p-8 shadow-xl">
        <h2 className="text-headline-lg font-bold text-on-surface mb-2">Verify OTP</h2>
        <p className="text-secondary mb-6">Enter the 4-digit security code sent to your email.</p>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-semibold mb-1 text-on-surface">Enter Code</label>
            <input
              type="text"
              maxLength={4}
              placeholder="1234"
              {...register("otp")}
              className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container text-center text-lg tracking-widest font-bold ${
                errors.otp ? "border-red-500" : "border-outline-variant"
              }`}
            />
            {errors.otp && (
              <span className="text-red-500 text-xs mt-1 block text-center">{errors.otp.message}</span>
            )}
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-primary-container text-white py-3 rounded-full font-button hover:bg-primary transition-all font-bold disabled:opacity-50"
          >
            {isSubmitting ? "Verifying..." : "Verify Security Code"}
          </button>
        </form>
      </div>
    </div>
  );
};

// 6. RESET PASSWORD
export const ResetPassword: React.FC = () => {
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm<ResetFormValues>({
    resolver: zodResolver(resetSchema),
    defaultValues: {
      password: "",
      confirm: ""
    }
  });

  const onSubmit = async (data: ResetFormValues) => {
    console.log("Reset password:", data);
    await new Promise((resolve) => setTimeout(resolve, 300));
    toast.success("Password reset successful!");
    navigate("/auth/reset-success");
  };

  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center px-4 py-12">
      <div className="w-full max-w-md bg-surface-container-lowest border border-outline-variant rounded-2xl p-8 shadow-xl">
        <h2 className="text-headline-lg font-bold text-on-surface mb-2">Reset Password</h2>
        <p className="text-secondary mb-6">Create a secure new password for your account.</p>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-semibold mb-1 text-on-surface">New Password</label>
            <input
              type="password"
              placeholder="Minimum 8 characters"
              {...register("password")}
              className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                errors.password ? "border-red-500" : "border-outline-variant"
              }`}
            />
            {errors.password && (
              <span className="text-red-500 text-xs mt-1 block">{errors.password.message}</span>
            )}
          </div>

          <div>
            <label className="block text-sm font-semibold mb-1 text-on-surface">Confirm New Password</label>
            <input
              type="password"
              placeholder="Confirm password"
              {...register("confirm")}
              className={`w-full px-4 py-2 border rounded-xl focus:outline-none focus:border-primary-container ${
                errors.confirm ? "border-red-500" : "border-outline-variant"
              }`}
            />
            {errors.confirm && (
              <span className="text-red-500 text-xs mt-1 block">{errors.confirm.message}</span>
            )}
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-primary-container text-white py-3 rounded-full font-button hover:bg-primary transition-all font-bold disabled:opacity-50"
          >
            {isSubmitting ? "Saving..." : "Save Password"}
          </button>
        </form>
      </div>
    </div>
  );
};

// 7. RESET SUCCESSFUL
export const ResetSuccess: React.FC = () => {
  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center px-4 py-12">
      <div className="w-full max-w-md bg-surface-container-lowest border border-outline-variant rounded-2xl p-8 shadow-xl text-center space-y-6">
        <div className="w-16 h-16 bg-tertiary-container text-white rounded-full flex items-center justify-center mx-auto text-3xl">
          <span className="material-symbols-outlined text-4xl">check_circle</span>
        </div>
        <h2 className="text-headline-lg font-bold text-on-surface">Reset Successful!</h2>
        <p className="text-secondary">Your password has been changed. You can now login with your new credentials.</p>
        <Link
          to="/auth/login"
          className="block w-full bg-primary-container text-white py-3 rounded-full font-button hover:bg-primary transition-all font-bold"
        >
          Login
        </Link>
      </div>
    </div>
  );
};

// 8. VERIFY EMAIL
export const VerifyEmail: React.FC = () => {
  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center px-4 py-12">
      <div className="w-full max-w-md bg-surface-container-lowest border border-outline-variant rounded-2xl p-8 shadow-xl text-center space-y-6">
        <div className="w-16 h-16 bg-primary-container/10 text-primary-container rounded-full flex items-center justify-center mx-auto text-3xl">
          <span className="material-symbols-outlined text-4xl">mail</span>
        </div>
        <h2 className="text-headline-lg font-bold text-on-surface">Verify Your Email</h2>
        <p className="text-secondary">
          We've sent a verification link to your registered email. Please click the link to confirm and complete registration.
        </p>
        <button
          onClick={() => {
            toast.success("Verification email resent!");
          }}
          className="w-full bg-white border border-outline-variant text-on-surface py-3 rounded-full font-button hover:bg-surface-container-low transition-all font-semibold"
        >
          Resend Verification Email
        </button>
        <Link
          to="/auth/login"
          className="block w-full bg-primary-container text-white py-3 rounded-full font-button hover:bg-primary transition-all font-bold text-center"
        >
          Proceed to Login
        </Link>
      </div>
    </div>
  );
};
