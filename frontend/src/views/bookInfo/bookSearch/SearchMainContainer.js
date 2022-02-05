import SearchMainPresenter from "./SearchMainPresenter";
import React, { useState,useEffect } from 'react'
import axios from "axios";
import SearchResultContainer from "./SearchResultContainer";

function SearchMainContainer(){
    const url = "http://i6a305.p.ssafy.io:8080";
    const [bestSellers, setBestSellers] = useState([]);
    useEffect(() => {
        getBestSellers();
        console.log('컴포넌트가 화면에 나타남');    
        return () => {
          console.log('컴포넌트가 화면에서 사라짐');
        };
      }, []);

      function goDetail(bookInfoSeq){
        let url= `/detail/${bookInfoSeq}`;
        document.location.href = url;
      }

    function getBestSellers(){ ///처음 시작할때
        axios.post(url+`/api/v1/bookinfos/` ,
        {
            page:1,
            limit:12,
            searchCategory : "title", 
            searchKeyword :"",  
            // searchKeyword : "", //이건 테스트 하기 위한거
            orderCategory: "latest"
          })
        .then(function (response){
          console.log(response.data.data.bookList);
            setBestSellers(response.data.data.bookList);
        })
        .catch(function (error) {
            console.log(error);
          }); 
    }

    return(
        <>
        <SearchMainPresenter goDetail={goDetail} bestSellers={bestSellers}></SearchMainPresenter>
        </>
    );
}


export {SearchMainContainer};