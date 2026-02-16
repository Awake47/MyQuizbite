package com.example.myquizbite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myquizbite.data.ThemeMode
import com.example.myquizbite.data.model.Difficulty
import com.example.myquizbite.data.model.UserRole
import com.example.myquizbite.navigation.NavRoutes
import com.example.myquizbite.ui.screen.*
import com.example.myquizbite.ui.theme.MyQuizbiteTheme
import com.example.myquizbite.ui.viewmodel.AdminViewModel
import com.example.myquizbite.ui.viewmodel.AuthViewModel
import com.example.myquizbite.ui.viewmodel.HomeViewModel
import com.example.myquizbite.ui.viewmodel.InterviewViewModel
import com.example.myquizbite.ui.viewmodel.ProfileViewModel
import com.example.myquizbite.ui.viewmodel.QuizRunViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as QuizBiteApp
            val themeMode by app.themeManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val isDark = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            MyQuizbiteTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val user by authViewModel.user.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = if (user != null) NavRoutes.Home else NavRoutes.Login,
                    modifier = Modifier
                ) {
                    composable(NavRoutes.Login) {
                        val error by authViewModel.authError.collectAsState()
                        LoginScreen(
                            onLogin = { e, p -> authViewModel.login(e, p) },
                            onNavigateToRegister = { navController.navigate(NavRoutes.Register) },
                            error = error,
                            onDismissError = { authViewModel.clearError() }
                        )
                    }
                    composable(NavRoutes.Register) {
                        val error by authViewModel.authError.collectAsState()
                        RegisterScreen(
                            onRegister = { e, p, n -> authViewModel.register(e, p, n) },
                            onNavigateBack = { navController.popBackStack() },
                            error = error,
                            onDismissError = { authViewModel.clearError() }
                        )
                    }
                    composable(NavRoutes.Home) {
                        val homeViewModel: HomeViewModel = viewModel()
                        LaunchedEffect(Unit) { homeViewModel.load() }
                        val topics by homeViewModel.topics.collectAsState()
                        val quizzes by homeViewModel.quizzes.collectAsState()
                        val dailyQuiz by homeViewModel.dailyQuiz.collectAsState()
                        val loading by homeViewModel.loading.collectAsState()
                        val streak by homeViewModel.streak.collectAsState()
                        val totalXp by homeViewModel.totalXp.collectAsState()
                        val newAchievements by homeViewModel.newAchievements.collectAsState()
                        HomeScreen(
                            userName = user?.displayName ?: "",
                            topics = topics,
                            quizzes = quizzes,
                            dailyQuiz = dailyQuiz,
                            loading = loading,
                            streak = streak,
                            totalXp = totalXp,
                            isAdmin = user?.role == UserRole.ADMIN,
                            newAchievements = newAchievements,
                            onRefresh = { homeViewModel.load() },
                            onQuizClick = { id -> navController.navigate(NavRoutes.quizRun(id)) },
                            onProfile = { navController.navigate(NavRoutes.Profile) },
                            onInterview = { navController.navigate(NavRoutes.Interview) },
                            onDuel = { navController.navigate(NavRoutes.Duel) },
                            onDailyChallenge = { dailyQuiz?.let { navController.navigate(NavRoutes.quizRun(it.id)) } ?: Unit },
                            onQuizList = { navController.navigate(NavRoutes.QuizList) },
                            onAdminPanel = { navController.navigate(NavRoutes.AdminPanel) },
                            onDismissAchievements = { homeViewModel.clearNewAchievements() }
                        )
                    }
                    composable(NavRoutes.Profile) {
                        val profileVm: ProfileViewModel = viewModel()
                        LaunchedEffect(user) { if (user != null) profileVm.load() }
                        val stats by profileVm.stats.collectAsState()
                        val streak by profileVm.streak.collectAsState()
                        val totalXp by profileVm.totalXp.collectAsState()
                        val achievements by profileVm.achievements.collectAsState()
                        val currentTheme by profileVm.themeMode.collectAsState()
                        ProfileScreen(
                            user = user,
                            stats = stats,
                            streak = streak,
                            totalXp = totalXp,
                            achievements = achievements,
                            currentTheme = currentTheme,
                            onThemeChanged = { profileVm.setThemeMode(it) },
                            onBack = { navController.popBackStack() },
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(NavRoutes.Login) { popUpTo(0) { inclusive = true } }
                            }
                        )
                    }
                    composable(NavRoutes.QuizList) {
                        val homeViewModel: HomeViewModel = viewModel()
                        LaunchedEffect(Unit) { homeViewModel.load() }
                        val quizzes by homeViewModel.quizzes.collectAsState()
                        val topics by homeViewModel.topics.collectAsState()
                        QuizListScreen(
                            topics = topics,
                            quizzes = quizzes,
                            onBack = { navController.popBackStack() },
                            onQuizClick = { id -> navController.navigate(NavRoutes.quizRun(id)) }
                        )
                    }
                    composable(NavRoutes.QuizRun) { backStackEntry ->
                        val quizId = backStackEntry.arguments?.getString("quizId") ?: return@composable
                        val vm: QuizRunViewModel = viewModel()
                        LaunchedEffect(quizId) { vm.startQuiz(quizId) }
                        val state by vm.state.collectAsState()
                        QuizRunScreen(
                            state = state,
                            onBack = { navController.popBackStack() },
                            onSelectAnswer = { vm.selectAnswer(it) },
                            onNext = { vm.nextQuestion() },
                            onTimeTick = { vm.setTimeLeft(it) },
                            onExit = { vm.reset(); navController.popBackStack(NavRoutes.Home, false) }
                        )
                    }
                    composable(NavRoutes.Interview) {
                        InterviewScreen(
                            onBack = { navController.popBackStack() },
                            onStartInterview = { d -> navController.navigate(NavRoutes.interviewRun(d.name)) },
                            onStartAdaptive = { navController.navigate(NavRoutes.interviewRun("adaptive")) }
                        )
                    }
                    composable(NavRoutes.InterviewRun) { backStackEntry ->
                        val mode = backStackEntry.arguments?.getString("mode") ?: "MEDIUM"
                        val interviewVm: InterviewViewModel = viewModel()
                        LaunchedEffect(mode) {
                            if (mode == "adaptive" && user != null) {
                                interviewVm.startInterviewAdaptive(user!!.id)
                            } else {
                                interviewVm.startInterview(Difficulty.valueOf(mode))
                            }
                        }
                        val state by interviewVm.state.collectAsState()
                        InterviewRunScreen(
                            state = state,
                            onBack = { navController.popBackStack() },
                            onSelectAnswer = { interviewVm.selectAnswer(it) },
                            onNext = { interviewVm.nextQuestion() },
                            onExit = { interviewVm.reset(); navController.popBackStack(NavRoutes.Home, false) }
                        )
                    }
                    composable(NavRoutes.Duel) {
                        DuelScreen(onBack = { navController.popBackStack() })
                    }
                    composable(NavRoutes.AdminPanel) {
                        val adminVm: AdminViewModel = viewModel()
                        LaunchedEffect(Unit) { adminVm.load() }
                        val stats by adminVm.stats.collectAsState()
                        val questions by adminVm.questions.collectAsState()
                        val quizzes by adminVm.quizzes.collectAsState()
                        val topics by adminVm.topics.collectAsState()
                        val message by adminVm.message.collectAsState()
                        AdminPanelScreen(
                            stats = stats,
                            questions = questions,
                            quizzes = quizzes,
                            topics = topics,
                            message = message,
                            onDeleteQuestion = { adminVm.deleteQuestion(it) },
                            onDeleteQuiz = { adminVm.deleteQuiz(it) },
                            onAddQuestion = { topicId, text, options, correct, explanation, difficulty, code ->
                                adminVm.addQuestion(topicId, text, options, correct, explanation, difficulty, code)
                            },
                            onAddQuiz = { topicId, title, desc, diff, qIds, time ->
                                adminVm.addQuiz(topicId, title, desc, diff, qIds, time)
                            },
                            onDismissMessage = { adminVm.clearMessage() },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                LaunchedEffect(user) {
                    if (user == null && navController.currentBackStackEntry?.destination?.route != NavRoutes.Login && navController.currentBackStackEntry?.destination?.route != NavRoutes.Register) {
                        navController.navigate(NavRoutes.Login) { popUpTo(0) { inclusive = true } }
                    }
                }
            }
        }
    }
}
