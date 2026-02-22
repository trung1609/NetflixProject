import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { email } from '@angular/forms/signals';
import { Router } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: false,
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class Landing {
  landingForm!: FormGroup;

  year = new Date().getFullYear();

  constructor(
    private fb: FormBuilder,
    private router: Router,
  ) {
    this.landingForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }
  login() {
    this.router.navigate(['/login']);
  }

  getStarted() {
    this.router.navigate(['/signup'], {
      queryParams: { email: this.landingForm.value.email },
    });
  }

  reasons = [
    {
      title: 'Enjoy on your TV.',
      text: 'Watch on Smart TVs, Playstation, Xbox, Apple TV, Blu-ray players and more.',
      icon: 'tv',
    },
    {
      title: 'Download your shows to watch offline.',
      text: 'Save your favorites easily and always have something to watch.',
      icon: 'file_download',
    },
    {
      title: 'Watch everywhere.',
      text: 'Stream unlimited movies and TV shows on your phone, tablet, laptop, and TV without paying more.',
      icon: 'devices',
    },
    {
      title: 'Create profiles for kids.',
      text: 'Send kids on adventures with their favorite characters in a space made just for them—free with your membership.',
      icon: 'face',
    },
  ];

  faqs = [
    {
      question: 'What is Netflix?',
      answer:
        'Netflix is a streaming service that offers a wide variety of award-winning TV shows, movies, anime, documentaries, and more on thousands of internet-connected devices. You can watch as much as you want, whenever you want without a single commercial – all for one low monthly price. There’s always something new to discover and new TV shows and movies are added every week!',
    },
    {
      question: 'How much does Netflix cost?',
      answer:
        'Watch Netflix on your smartphone, tablet, Smart TV, laptop, or streaming device, all for one fixed monthly fee. Plans range from ₹149 to ₹649 a month. No extra costs, no contracts.',
    },
    {
      question: 'Where can I watch?',
      answer:
        "Watch anywhere, anytime. Sign in with your Netflix account to watch instantly on the web at netflix.com from your personal computer or on any internet-connected device that offers the Netflix app, including smart TVs, smartphones, tablets, streaming media players and game consoles. You can also download your favorite shows with the iOS, Android, or Windows 10 app. Use downloads to watch while you're on the go and without an internet connection. Take Netflix with you anywhere.",
    },
    {
      question: 'How do I cancel?',
      answer:
        'Netflix is flexible. There are no pesky contracts and no commitments. You can easily cancel your account online in two clicks. There are no cancellation fees – start or stop your account anytime.',
    },
    {
      question: 'What can I watch on Netflix?',
      answer:
        'Netflix has an extensive library of feature films, documentaries, TV shows, anime, award-winning Netflix originals, and more. Watch as much as you want, anytime you want.',
    },
    {
      question: 'Is Netflix good for kids?',
      answer:
        "The Netflix Kids experience is included in your membership to give parents control while kids enjoy family-friendly TV shows and movies in their own space. Kids profiles come with PIN-protected parental controls that let you restrict the maturity rating of content kids can watch and block specific titles you don't want kids to see.",
    },
    {
      question: 'Why am I seeing this language?',
      answer:
        'Your browser preferences determine the language shown when you visit Netflix. To change the language, adjust your browser preferences to prioritize the language you prefer.',
    },
  ];
}
