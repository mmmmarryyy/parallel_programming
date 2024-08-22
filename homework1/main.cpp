#include <iostream>
#include <fstream>
#include <pthread.h>
#include "pthread_barrier.h" //https://github.com/isotes/pthread-barrier-macos/tree/master
#include <random>
#include <unistd.h>
#include <csignal>

std::sig_atomic_t interrupted = 0;

void sigterm_signal_handler(int sig) {
    (void)sig;
    // std::cout << "interrupted; sig = " << sig << std::endl;
    interrupted = 1;
}

int32_t number_of_consumers;
int32_t sleeping_time;

std::vector<pthread_t> consumer_threads;
pthread_t producer, interruptor;

thread_local int result_local_thread_sum = 0;

pthread_mutex_t pmutex = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t pcond = PTHREAD_COND_INITIALIZER;

pthread_mutex_t cmutex = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t ccond = PTHREAD_COND_INITIALIZER;

pthread_barrier_t barrier;

// generating random numbers
std::random_device rd;
std::mt19937 seed(rd());
std::uniform_int_distribution<uint64_t> gen_random_number(0, (uint64_t)-1);

bool cflag = false;
bool pflag = true;

void* producer_routine(void* arg) {
    int* number = (int*)arg;
    std::ifstream ifs("in.txt");

    pthread_barrier_wait(&barrier);

    while (ifs >> *number && !interrupted) {
        // std::cout << *number << std::endl;
        pthread_mutex_lock(&cmutex);

        cflag = true;
        // std::cout << "cflag = " << cflag << std::endl;

        pthread_cond_signal(&ccond);
        pthread_mutex_unlock(&cmutex);
        pthread_mutex_lock(&pmutex);

        while (!interrupted && cflag) {
            pthread_cond_wait(&pcond, &pmutex);
        }

        // std::cout << "after while (!interrupted && cflag)" << std::endl;
        pthread_mutex_unlock(&pmutex);
        // std::cout << "endl of while, interrupted = " << interrupted << std::endl << std::endl;
    }

    pthread_mutex_lock(&cmutex);

    pflag = false;

    pthread_cond_broadcast(&ccond);
    pthread_mutex_unlock(&cmutex);

    return NULL;
}

void* consumer_routine(void* arg) {
    pthread_setcancelstate(PTHREAD_CANCEL_DISABLE, NULL);
    int* number = (int*)arg;

    pthread_barrier_wait(&barrier);

    while (!interrupted && pflag) {
        pthread_mutex_lock(&cmutex);

        while (!interrupted && pflag && !cflag) {
            pthread_cond_wait(&ccond, &cmutex);
        }

        if (!interrupted && pflag) {
            pthread_mutex_lock(&pmutex);

            result_local_thread_sum += *number;
            cflag = false;

            pthread_cond_signal(&pcond);
            pthread_mutex_unlock(&pmutex);
        }

        pthread_mutex_unlock(&cmutex);

        usleep((gen_random_number(seed) % sleeping_time) * 1000);
    }

    return new int(result_local_thread_sum);
}

void* consumer_interruptor_routine(void* arg) {
    pthread_barrier_wait(&barrier);

    (void)arg;

    while (!interrupted && pflag) {
        pthread_cancel(consumer_threads[gen_random_number(seed) % consumer_threads.size()]);
    }

    return NULL;
}

int run_threads() {
    int status_of_barrier_init = pthread_barrier_init(&barrier, NULL, number_of_consumers + 2);

    if (status_of_barrier_init != 0) {
        std::cerr << "main error: can't init barrier, status = " << status_of_barrier_init << std::endl;
        exit(1);
    }

    int number;

    for (auto& consumer: consumer_threads) {
        pthread_create(&consumer, NULL, consumer_routine, &number);
    }

    pthread_create(&producer, NULL, producer_routine, &number);
    pthread_create(&interruptor, NULL, consumer_interruptor_routine, NULL);

    int result_sum = 0;
    pthread_join(interruptor, NULL);

    for (size_t i = 0; i < consumer_threads.size(); i++) {
        void* thread_result_sum;
        pthread_join(consumer_threads[i], &thread_result_sum);
        // std::cout << "join thread number " << i << std::endl;
        result_sum += *(int*)thread_result_sum;
        delete (int*)thread_result_sum;
    }

    return result_sum;
}

int main(int argc, char** argv) {
    if (argc != 3) {
        std::cerr << "You provide wrong number of arguments" << std::endl;
        return 1;
    }

    number_of_consumers = atoi(argv[1]);

    if (number_of_consumers < 1) {
        std::cerr << "number of consumer threads should be at least 1" << std::endl;
        return 1;
    }

    sleeping_time = atoi(argv[2]);

    if (sleeping_time < 0) {
        std::cerr << "sleeping time should be at least 0" << std::endl;
        return 1;
    }

    sleeping_time++;
    consumer_threads.resize(number_of_consumers);

    //here should be SIGTERM
    std::signal(SIGTERM, sigterm_signal_handler);

    std::cout << run_threads() << std::endl;
    return 0;
}