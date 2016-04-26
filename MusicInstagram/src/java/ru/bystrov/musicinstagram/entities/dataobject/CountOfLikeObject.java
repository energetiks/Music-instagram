/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.bystrov.musicinstagram.entities.dataobject;

/**
 *
 * @author Test
 */
public class CountOfLikeObject implements Comparable {
    private Integer countOfLike;
    private Integer countOfDisLike;
    private Long objId;

    public Integer getCountOfLike() {
        return countOfLike;
    }

    public void setCountOfLike(Integer countOfLike) {
        this.countOfLike = countOfLike;
    }

    public Long getObjId() {
        return objId;
    }

    public void setObjId(Long objId) {
        this.objId = objId;
    }

    public CountOfLikeObject(Integer countOfLike, Integer countOfDisLike, Long objId){
        this.countOfLike = countOfLike;
        this.countOfDisLike = countOfDisLike;
        this.objId = objId;
    }

    public CountOfLikeObject() {
    }

    /* Перегрузка метода compareTo */
                       
    @Override
  public int compareTo(Object obj)
  {
    CountOfLikeObject object = (CountOfLikeObject) obj;
    if(this.countOfLike < object.countOfLike) {
      return -1;
    }   
    else if(this.countOfLike > object.countOfLike) {
      return 1;
    } else
    {
        if(this.countOfDisLike > object.countOfDisLike) {
          return -1;
        }   
        else if(this.countOfDisLike < object.countOfDisLike) {
          return 1;
        } else {
            if(this.objId < object.objId) {
                return -1;
              }   
              else if(this.objId > object.objId) {
                return 1;
              }
        }
    }
    
    return 0;  
  }

    public Integer getCountOfDisLike() {
        return countOfDisLike;
    }

    public void setCountOfDisLike(Integer countOfDisLike) {
        this.countOfDisLike = countOfDisLike;
    }
    
    
}
