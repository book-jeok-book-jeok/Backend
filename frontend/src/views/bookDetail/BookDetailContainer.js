import BookDetailPresenter from "./BookDetailPresenter"; 
import React, { useState,useEffect, useReducer } from "react";
import axios from "axios";
import { useParams } from "react-router-dom";
import { useSelector } from 'react-redux';

function BookDetailContainer(bookInfoSeq){
        const user=useSelector(state => state.authReducer);
        const jwtToken = JSON.parse(sessionStorage.getItem("jwtToken"));

        let useParam=useParams();
        const url = "https://i6a305.p.ssafy.io:8443";

        const [title, setTitle]= useState();
        const [author, setAuthor]= useState();
        const [image, setImage]= useState();
        const [publisher, setPublisher]= useState();
        const [publicationDate, setPublicationDate]= useState();
        const [seq, setSeq]= useState();
        
        const [reviews, setReviews]= useState();
        const [starRating, setStarRating]= useState();

        const [reviewPage, setReviewPage]= useState(1);
        const [reviewTotalCnt, setReviewTotalCnt]= useState(1);

        const [booklogs, setBooklogs]= useState();
        const [booklogPage, setBooklogPage]= useState(1);
        const [booklogOrder, setBooklogOrder]= useState("recent");
        const [booklogTotalCnt, setBooklogTotalCnt]= useState();

        /////로그인한 사용자 정보 땡겨오는거부터 하자!!!!!!!
        const [MyModalOpen, setMyModalOpen] = useState(false);
        const [WriteModalOpen, setWriteModalOpen]= useState(false);

        const [userReview, setUserReview]= useState({
              starRating:"",
              summary:"",
              createdDate:"",
              bookReviewSeq:"",
              memberInfo:""
          });


        useEffect(() => {
            getBookInfo();
            getBookReview();
            getBookLog();
            console.log('컴포넌트가 화면에 나타남');
            return () => {
              console.log('컴포넌트가 화면에서 사라짐');
            };
          }, []);

          useEffect(() => {
            getBookReview();
            return () => {
            };
          }, [reviewPage]);

          useEffect(() => {
            getBookLog();
            return () => {
            };
          }, [booklogPage]);


          useEffect(() => {
            getBookLog();
            return () => {
            };
          }, [booklogOrder]);
        
        function getBookInfo(){

            //책정보 데려오기
            axios.get(url+`/api/v1/bookinfos/${useParam.seq}`)
            .then(function (response){
                setSeq(response.data.data.bookInfo.seq);
                setImage(response.data.data.bookInfo.largeImgUrl);
                setTitle(response.data.data.bookInfo.title);
                setAuthor(response.data.data.bookInfo.author);
                setPublisher(response.data.data.bookInfo.publisher);
                setPublicationDate(response.data.data.bookInfo.publicationDate);
                //setStarRating(response.data.data.bookInfo.starRating);
            })
            .catch(function (error) {
                console.log(error);
              }); 
              
        }
        

        useEffect(() => {
          getUserReview();
          return () => {
          };
        }, [reviews]);  

        useEffect(() => {
          putUserReview();
          return () => {
          };
        }, [userReview]);  

        function putUserReview(){
          console.log(userReview);
        }
        
        function getBookReview(){
            //책 리뷰 가져오기
            axios.get(url+`/api/v1/bookreviews/bookinfos/${useParam.seq}`)
            .then(function (response){

              console.log(response.data.data);
              setReviews(response.data.data.myBookReviews);
              setReviewTotalCnt(response.data.data.myBookReviews.totalCnt);
              })

            .catch(function (error) {
                console.log(error);
              }); 
        }

        function getUserReview(){
          console.log(reviews);
          if(reviews!==undefined){
            for(let i=0; i<reviews.length; i++){
              if(reviews[i].memberSeq===user.memberInfo.seq){
                console.log(reviews[i]);
                setUserReview({
                  starRating: reviews[i].starRating,
                  summary:reviews[i].summary,
                  createdDate: reviews[i].createdDate,
                  bookReviewSeq: reviews[i].bookReviewSeq,
                  memberInfo: reviews[i].memberSeq
                });
              }
            }
          }

        }

        function reviewPageHandler(event){
            setReviewPage(event);
        }
        
        function booklogPageHandler(event){
            setBooklogPage(event);
        }
        
        function booklogOrderHandler(event){
            setBooklogOrder(event.target.value);
        }
        
        function getBookLog(){
            //북로그 가져오기
            axios.get(url+`/api/v1/booklogs?page=${booklogPage}&size=5&sort=${booklogOrder}`)
            .then(function (response){
                //console.log(response.data.data.booklogs.content);
                setBooklogs(response.data.data.booklogs);
                setBooklogTotalCnt(response.data.data.totalCnt);
            })  
            .catch(function (error) {
                console.log(error);
              }); 
        
        }

        function handleMyModalOpen(){
          if(jwtToken==="" ||jwtToken===undefined|| jwtToken===null){
            alert("로그인 후 사용할 수 있습니다.")
          }
          else{
            setMyModalOpen(true);
          }
        }

        function handleWriteModalOpen(){
          if(jwtToken===""||jwtToken===undefined|| jwtToken===null){
            alert("로그인 후 사용할 수 있습니다.")
          }else{
            setWriteModalOpen(true);
          }

        }
        function handleMyModalClose(){
          setMyModalOpen(false);
        }
        function handleWriteModalClose(){
          setWriteModalOpen(false);
        }
    return(
        <BookDetailPresenter 
        reviewPage={reviewPage} reviewTotalCnt={reviewTotalCnt}reviews={reviews} reviewPageHandler={reviewPageHandler} 
        booklogs={booklogs} booklogPage={booklogPage} booklogPageHandler={booklogPageHandler} booklogOrderHandler={booklogOrderHandler} booklogTotalCnt={booklogTotalCnt}
        starRating={starRating} image={image} title ={title} author={author} publisher={publisher} publicationDate={publicationDate}
        handleMyModalClose={handleMyModalClose} handleWriteModalClose={handleWriteModalClose}
        setMyModalOpen={setMyModalOpen} setWriteModalOpen={setWriteModalOpen}
        handleMyModalOpen={handleMyModalOpen} handleWriteModalOpen={handleWriteModalOpen}
        MyModalOpen={MyModalOpen} WriteModalOpen={WriteModalOpen}
        userReview={userReview}
        bookInfoSeq={useParam.seq}
        starRating={starRating}
        user={user} jwtToken={jwtToken}
        seq={seq} url={url}
        ></BookDetailPresenter>

        );
}
export default BookDetailContainer;